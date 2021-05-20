package com.minecolonies.coremod.colony.buildings.modules;

import com.ldtteam.blockout.Color;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.AbstractCitizenAssignable;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenBornEvent;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.TWENTYFIVESEC;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RESIDENTS;

/**
 * The class of the citizen hut.
 */
public class LivingBuildingModule extends AbstractBuildingModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule
{
    /**
     * Is a female citizen living here?
     */
    private boolean femalePresent = false;

    /**
     * Is a male citizen living here?
     */
    private boolean malePresent = false;

    /**
     * The time in seconds before the initial try to spawn
     */
    private static final int MIN_TIME_BEFORE_SPAWNTRY = 300;

    /**
     * Interval at which the childen are created, in ticks. Every 20 min it tries to spawn a child, 20min*60s*20ticks
     */
    private final static int CHILD_SPAWN_INTERVAL = 20 * 60;

    /**
     * The timer counting ticks to the next time creating a child
     */
    private int childCreationTimer;

    /**
     * Creates a new home building module.
     */
    public LivingBuildingModule()
    {
        final Random rand = new Random();
        childCreationTimer = rand.nextInt(CHILD_SPAWN_INTERVAL) + MIN_TIME_BEFORE_SPAWNTRY;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        if (compound.keySet().contains(TAG_RESIDENTS))
        {
            final int[] residentIds = compound.getIntArray(TAG_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final ICitizenData citizen = building.getColony().getCitizenManager().getCivilian(citizenId);
                if (citizen != null)
                {
                    assignCitizen(citizen);
                }
            }
        }
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        if (building.hasAssignedCitizen())
        {
            @NotNull final int[] residentIds = new int[building.getAssignedCitizen().size()];
            for (int i = 0; i < building.getAssignedCitizen().size(); ++i)
            {
                residentIds[i] = building.getAssignedCitizen().get(i).getId();
            }
            compound.putIntArray(TAG_RESIDENTS, residentIds);
        }
    }

    @Override
    public void onDestroyed()
    {
        building.getAssignedCitizen().stream()
          .filter(Objects::nonNull)
          .forEach(citizen -> citizen.setHomeBuilding(null));
    }

    @Override
    public boolean removeCitizen(@NotNull final ICitizenData citizen)
    {
        if (building.isCitizenAssigned(citizen))
        {
            ((AbstractCitizenAssignable) building).removeAssignedCitizen(citizen);
            building.getColony().getCitizenManager().calculateMaxCitizens();
            markDirty();
            citizen.setHomeBuilding(null);

            femalePresent = false;
            malePresent = false;

            for (final ICitizenData citizenData : building.getAssignedCitizen())
            {
                if (citizenData.isFemale())
                {
                    femalePresent = true;
                }
                else
                {
                    malePresent = true;
                }

                if (femalePresent && malePresent)
                {
                    return true;
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (building.getBuildingLevel() > 0 && (childCreationTimer -= TWENTYFIVESEC) <= 0)
        {
            childCreationTimer =
              (colony.getWorld().rand.nextInt(500) + CHILD_SPAWN_INTERVAL * (colony.getCitizenManager().getCurrentCitizenCount() / Math.max(4,
                colony.getCitizenManager()
                  .getMaxCitizens())));
            trySpawnChild();
        }

        if (!building.isFull() && !building.getColony().isManualHousing())
        {
            // 'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    /**
     * Try to spawn a new citizen as child. Mom / dad entities are required and chosen randomly in this hut. Childs inherit stats from their parents, avergaged +-2 Childs get
     * assigned to a free housing slot in the colony to be raised there, if the house has an adult living there the child takes its name and gets raised by it.
     */
    public void trySpawnChild()
    {
        // Spawn a child when adults are present
        if (building.getColony().canMoveIn() && femalePresent && malePresent && building.getColony().getCitizenManager().getCurrentCitizenCount() < building.getColony().getCitizenManager().getMaxCitizens())
        {
            ICitizenData mom = null;
            ICitizenData dad = null;

            for (final ICitizenData data : building.getAssignedCitizen())
            {
                if (data.isFemale() && !data.isChild())
                {
                    mom = data;
                }
                else if (!data.isChild())
                {
                    dad = data;
                }

                if (mom != null && dad != null)
                {
                    break;
                }
            }

            if (mom == null || dad == null)
            {
                return;
            }

            final ICitizenData newCitizen = building.getColony().getCitizenManager().createAndRegisterCivilianData();

            final Random rand = new Random();

            newCitizen.getCitizenSkillHandler().init(mom, dad, rand);
            newCitizen.setIsChild(true);

            // Assign citizen to a house
            if (assignCitizen(newCitizen))
            {
                if (rand.nextInt(2) == 1)
                {
                    inheritLastName(newCitizen, mom.getName());
                }
                else
                {
                    inheritLastName(newCitizen, dad.getName());
                }
            }
            else
            {
                // Assign to a different citizen hut and adopt
                for (final IBuilding build : building.getColony().getBuildingManager().getBuildings().values())
                {
                    if (!(build instanceof LivingBuildingModule))
                    {
                        continue;
                    }

                    // Try assigning
                    if (build.assignCitizen(newCitizen))
                    {
                        // Get Parent Name
                        for (final ICitizenData data : build.getAssignedCitizen())
                        {
                            // Exclude child itself
                            if (data.getId() != newCitizen.getId())
                            {
                                inheritLastName(newCitizen, data.getName());
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            // TODO: evaluate, consider similar 'genetics' for eye color, hair color, moving into the initializer for the new citizen data?
            final int momSuffix = mom.getHairColor();
            final int dadSuffix = dad.getHairColor();
            int red = 0;
            int green = 0;
            int blue = 0;
            // Put a little weighting into the resulting suffix color, to avoid gravitating toward middle values too quickly colonies without a Tavern.
            final int weighting = rand.nextInt(10);
            for(int i = 0; i < weighting; i++)
            {
                red += momSuffix & 0xFF0000;
                green += momSuffix & 0xFF00;
                blue += momSuffix & 0xFF;
            }
            for(int i = 0; i < 10 - weighting; i++)
            {
                red += dadSuffix & 0xFF0000;
                green += dadSuffix & 0xFF00;
                blue += dadSuffix & 0xFF;
            }
            newCitizen.setSuffix(Color.rgbaToInt((int)(red / 10f), (int)(green / 10f), (int)(blue / 10f), 0));

            final int populationCount = building.getColony().getCitizenManager().getCurrentCitizenCount();
            AdvancementUtils.TriggerAdvancementPlayersForColony(building.getColony(), playerMP -> AdvancementTriggers.COLONY_POPULATION.trigger(playerMP, populationCount));

            LanguageHandler.sendPlayersMessage(building.getColony().getImportantMessageEntityPlayers(), "com.minecolonies.coremod.progress.newChild", newCitizen.getName(), building.getColony().getName());
            building.getColony().getCitizenManager().spawnOrCreateCitizen(newCitizen, building.getColony().getWorld(), building.getPosition());

            building.getColony().getEventDescriptionManager().addEventDescription(new CitizenBornEvent(building.getPosition(), newCitizen.getName()));
        }
    }

    /**
     * Inherit the last name of a parent
     *
     * @param child      The child who inherits the name
     * @param parentName the parents name to inherit
     */
    private void inheritLastName(@NotNull final ICitizenData child, final String parentName)
    {
        if (parentName == null || parentName.split(" ").length < 2 || child.getName().split(" ").length < 2)
        {
            return;
        }

        String[] newName = child.getName().split(" ");
        final String[] lastName = parentName.split(" ");
        newName[newName.length - 1] = lastName[lastName.length - 1];

        final StringBuilder combinedName = new StringBuilder();
        for (final String namePart : newName)
        {
            combinedName.append(namePart).append(" ");
        }
        child.setName(combinedName.toString().trim());
    }

    /**
     * Looks for a homeless citizen to add to the current building Calls. {@link #assignCitizen(ICitizenData)}
     */
    private void addHomelessCitizens()
    {
        // Prioritize missing genders for assigning
        if (!malePresent || !femalePresent)
        {
            for (@NotNull final ICitizenData citizen : building.getColony().getCitizenManager().getCitizens())
            {
                if (building.isFull())
                {
                    break;
                }
                if (femalePresent && citizen.isFemale())
                {
                    continue;
                }
                if (malePresent && !citizen.isFemale())
                {
                    continue;
                }
                moveCitizenToHut(citizen);
            }
        }

        for (@NotNull final ICitizenData citizen : building.getColony().getCitizenManager().getCitizens())
        {
            if (building.isFull())
            {
                break;
            }
            moveCitizenToHut(citizen);
        }
    }

    /**
     * Moves the citizen into his new hut
     *
     * @param citizen the citizen to move
     */
    private void moveCitizenToHut(final ICitizenData citizen)
    {
        // Move the citizen to a better hut
        if (citizen.getHomeBuilding() instanceof LivingBuildingModule && citizen.getHomeBuilding().getBuildingLevel() < building.getBuildingLevel())
        {
            citizen.getHomeBuilding().removeCitizen(citizen);
        }
        if (citizen.getHomeBuilding() == null)
        {
            assignCitizen(citizen);
        }
    }

    @Override
    public boolean assignCitizen(final ICitizenData citizen)
    {
        if (citizen.getHomeBuilding() != null)
        {
            citizen.getHomeBuilding().removeCitizen(citizen);
        }

        if (!buildingAssignmentLogic(citizen))
        {
            return false;
        }

        // Set for childen creation
        if (citizen.isFemale())
        {
            femalePresent = true;
        }
        else
        {
            malePresent = true;
        }

        citizen.setHomeBuilding(building);
        return true;
    }

    @Override
    public int getModuleMax()
    {
        return building.getMaxInhabitants();
    }

    private boolean buildingAssignmentLogic(final ICitizenData citizen)
    {
        if (building.getAssignedCitizen().contains(citizen) || building.isFull())
        {
            return false;
        }

        // If we set a worker, inform it of such
        if (citizen != null)
        {
            ((AbstractCitizenAssignable) building).addAssignedCitizen(citizen);
        }

        building.getColony().getCitizenManager().calculateMaxCitizens();
        markDirty();
        return true;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        for (final Optional<AbstractEntityCitizen> entityCitizen : Objects.requireNonNull(building.getAssignedEntities()))
        {
            if (entityCitizen.isPresent() && entityCitizen.get().getCitizenJobHandler().getColonyJob() == null)
            {
                entityCitizen.get().getCitizenJobHandler().setModelDependingOnJob(null);
            }
        }
        building.getColony().getCitizenManager().calculateMaxCitizens();
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(building.getAssignedCitizen().size());
        for (@NotNull final ICitizenData citizen : building.getAssignedCitizen())
        {
            buf.writeInt(citizen.getId());
        }
    }

    @Override
    public void onBuildingMove(final IBuilding oldBuilding)
    {
        final List<ICitizenData> residents = oldBuilding.getAssignedCitizen();
        for (final ICitizenData citizen : residents)
        {
            citizen.setHomeBuilding(building);
            this.assignCitizen(citizen);
        }
    }
}
