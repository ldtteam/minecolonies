package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.IReproductionManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.CitizenBornEvent;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.coremod.colony.CitizenData.SUFFIXES;

/**
 * Repo manager to spawn children.
 */
public class ReproductionManager implements IReproductionManager
{
    /**
     * The time in seconds before the initial try to spawn. 5 minutes between each attempt.
     */
    private static final int MIN_TIME_BEFORE_SPAWNTRY = 20 * 60 * 5;

    /**
     * Interval at which the childen are created, in ticks. Every 10 min it tries to spawn a child.
     */
    private final static int CHILD_SPAWN_INTERVAL = 20 * 60 * 10;

    /**
     * Min necessary citizens for reproduction.
     */
    private static final int MIN_SIZE_FOR_REPRO   = 2;

    /**
     * The timer counting ticks to the next time creating a child
     */
    private int childCreationTimer;

    /**
     * The colony the manager belongs to.
     */
    private final Colony colony;

    /**
     * Random function for the manager to use.
     */
    private Random random = new Random();

    /**
     * Create a new reproduction manager.
     * @param colony the colony to spawn kids for.
     */
    public ReproductionManager(final Colony colony)
    {
        this.colony = colony;
        childCreationTimer = random.nextInt(CHILD_SPAWN_INTERVAL) + MIN_TIME_BEFORE_SPAWNTRY;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if ( (childCreationTimer -= MAX_TICKRATE) <= 0)
        {
            childCreationTimer = (MIN_TIME_BEFORE_SPAWNTRY + random.nextInt(CHILD_SPAWN_INTERVAL)) * (colony.getCitizenManager().getCurrentCitizenCount() / Math.max(4, colony.getCitizenManager().getMaxCitizens()));
            trySpawnChild();
        }
    }

    /**
     * Try to spawn a new citizen as child. Mom / dad entities are required and chosen randomly in this hut. Childs inherit stats from their parents, avergaged +-2 Childs get
     * assigned to a free housing slot in the colony to be raised there, if the house has an adult living there the child takes its name and gets raised by it.
     */
    public void trySpawnChild()
    {
        // Spawn a child when adults are present
        if (colony.canMoveIn() && colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getMaxCitizens() && colony.getCitizenManager().getCurrentCitizenCount() >= Math.min(MIN_SIZE_FOR_REPRO, MinecoloniesAPIProxy.getInstance().getConfig().getServer().initialCitizenAmount.get()))
        {
            if (!checkForBioParents())
            {
                return;
            }

            final IBuilding newHome = colony.getBuildingManager().getHouseWithSpareBed();
            if (newHome == null)
            {
                return;
            }

            final List<ICitizenData> assignedCitizens = newHome.getAssignedCitizen();
            assignedCitizens.removeIf(ICitizen::isChild);

            final ICitizenData newCitizen = colony.getCitizenManager().createAndRegisterCivilianData();
            ICitizenData firstParent;
            ICitizenData secondParent;
            if (!assignedCitizens.isEmpty())
            {
                firstParent = assignedCitizens.get(random.nextInt(assignedCitizens.size()));
                secondParent = firstParent.getPartner();
                if (secondParent == null)
                {
                    assignedCitizens.removeIf(cit -> cit.getPartner() != null || cit.getName().equals(firstParent.getName()) || cit.isRelatedTo(firstParent));
                    if (assignedCitizens.size() > 0 && random.nextBoolean())
                    {
                        secondParent = assignedCitizens.get(random.nextInt(assignedCitizens.size()));
                    }
                    else
                    {
                        final BlockPos altPos = colony.getBuildingManager().getRandomBuilding(b -> b.hasModule(LivingBuildingModule.class) && !b.getPosition().equals(newHome.getPosition()) && BlockPosUtil.getDistance2D(b.getPosition(), newHome.getPosition()) < 50);
                        if (altPos != null)
                        {
                            final IBuilding building = colony.getBuildingManager().getBuilding(altPos);
                            final List<ICitizenData> newAssignedCitizens = building.getAssignedCitizen();
                            newAssignedCitizens.removeIf(cit -> cit.isChild() || cit.getPartner() != null || cit.isRelatedTo(firstParent));
                            if (newAssignedCitizens.size() > 0)
                            {
                                secondParent = newAssignedCitizens.get(random.nextInt(newAssignedCitizens.size()));
                            }
                        }
                    }
                }
            }
            else
            {
                firstParent = null;
                secondParent = null;
            }

            if (secondParent != null)
            {
                firstParent.setPartner(secondParent.getId());
                secondParent.setPartner(firstParent.getId());
            }

            newCitizen.getCitizenSkillHandler().init(colony, firstParent, secondParent, random);
            newCitizen.setIsChild(true);

            final List<String> possibleSuffixes = new ArrayList<>();
            if (firstParent != null)
            {
                newCitizen.addSiblings(firstParent.getChildren().toArray(new Integer[0]));
                firstParent.addChildren(newCitizen.getId());
                possibleSuffixes.add(firstParent.getTextureSuffix());
            }

            if (secondParent != null)
            {
                newCitizen.addSiblings(secondParent.getChildren().toArray(new Integer[0]));
                secondParent.addChildren(newCitizen.getId());
                possibleSuffixes.add(secondParent.getTextureSuffix());
            }

            newCitizen.setParents(firstParent == null ? "" : firstParent.getName(), secondParent == null ? "" : secondParent.getName());
            newCitizen.generateName(random, firstParent == null ? "" : firstParent.getName(), secondParent == null ? "" : secondParent.getName());

            newHome.assignCitizen(newCitizen);

            for (final int sibling : newCitizen.getSiblings())
            {
                final ICitizenData siblingData = colony.getCitizenManager().getCivilian(sibling);
                if (siblingData != null)
                {
                    siblingData.addSiblings(newCitizen.getId());
                }
            }

            if (possibleSuffixes.contains("_w") && possibleSuffixes.contains("_d"))
            {
                possibleSuffixes.add("_b");
            }

            if (possibleSuffixes.isEmpty())
            {
                possibleSuffixes.addAll(SUFFIXES);
            }

            newCitizen.setSuffix(possibleSuffixes.get(random.nextInt(possibleSuffixes.size())));

            final int populationCount = colony.getCitizenManager().getCurrentCitizenCount();
            AdvancementUtils.TriggerAdvancementPlayersForColony(colony, playerMP -> AdvancementTriggers.COLONY_POPULATION.trigger(playerMP, populationCount));

            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), "com.minecolonies.coremod.progress.newChild", newCitizen.getName(), colony.getName());
            colony.getCitizenManager().spawnOrCreateCitizen(newCitizen, colony.getWorld(), newHome.getPosition());

            colony.getEventDescriptionManager().addEventDescription(new CitizenBornEvent(newHome.getPosition(), newCitizen.getName()));
        }
    }

    /**
     * Check if there are potential biological parents in the colony.
     * (At least one male/female citizen).
     * @return true if so.
     */
    private boolean checkForBioParents()
    {
        boolean hasMale = false;
        boolean hasFemale = false;

        for (final ICitizenData data : colony.getCitizenManager().getCitizens())
        {
            if (data.isFemale())
            {
                hasFemale = true;
            }
            else
            {
                hasMale = true;
            }

            if (hasFemale && hasMale)
            {
                return true;
            }
        }

        for (final ICivilianData data : colony.getVisitorManager().getCivilianDataMap().values())
        {
            if (data.isFemale())
            {
                hasFemale = true;
            }
            else
            {
                hasMale = true;
            }

            if (hasFemale && hasMale)
            {
                return true;
            }
        }
        return false;
    }
}
