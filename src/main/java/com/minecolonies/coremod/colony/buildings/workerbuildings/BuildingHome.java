package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutCitizen;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.Constants.TWENTYFIVESEC;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BEDS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RESIDENTS;

/**
 * The class of the citizen hut.
 */
public class BuildingHome extends AbstractBuilding
{
    /**
     * The string describing the hut.
     */
    private static final String CITIZEN = "Citizen";

    /**
     * List of all bedList.
     */
    @NotNull
    private final List<BlockPos> bedList = new ArrayList<>();

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
     * Interval at which the childen are created, in ticks.
     * Every 20 min it tries to spawn a child, 20min*60s*20ticks
     */
    private int childCreationInterval = 600;

    /**
     * The timer counting ticks to the next time creating a child
     */
    private int childCreationTimer;

    /**
     * Defines how much child stats very from parents average(+ or -).
     */
    private static final int CHILD_STATS_VARIANCE = 3;

    /**
     * Instantiates a new citizen hut.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingHome(final IColony c, final BlockPos l)
    {
        super(c, l);
        final Random rand = new Random();
        childCreationTimer = rand.nextInt(childCreationInterval) + MIN_TIME_BEFORE_SPAWNTRY;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);

        if (compound.hasKey(TAG_RESIDENTS))
        {
            final int[] residentIds = compound.getIntArray(TAG_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final ICitizenData citizen = getColony().getCitizenManager().getCitizen(citizenId);
                if (citizen != null)
                {
                    // Bypass assignCitizen (which marks dirty)
                    assignCitizen(citizen);
                }
            }
        }

        final NBTTagList bedTagList = compound.getTagList(TAG_BEDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < bedTagList.tagCount(); ++i)
        {
            final NBTTagCompound bedCompound = bedTagList.getCompoundTagAt(i);
            final BlockPos bedPos = NBTUtil.getPosFromTag(bedCompound);
            if (!bedList.contains(bedPos))
            {
                bedList.add(bedPos);
            }
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        if (hasAssignedCitizen())
        {
            @NotNull final int[] residentIds = new int[getAssignedCitizen().size()];
            for (int i = 0; i < getAssignedCitizen().size(); ++i)
            {
                residentIds[i] = getAssignedCitizen().get(i).getId();
            }
            compound.setIntArray(TAG_RESIDENTS, residentIds);
        }
        if (!bedList.isEmpty())
        {
            @NotNull final NBTTagList bedTagList = new NBTTagList();
            for (@NotNull final BlockPos pos : bedList)
            {
                bedTagList.appendTag(NBTUtil.createPosTag(pos));
            }
            compound.setTag(TAG_BEDS, bedTagList);
        }

        return compound;
    }

    @Override
    public void onWakeUp()
    {
        final World world = getColony().getWorld();
        if (world == null)
        {
            return;
        }

        for (final BlockPos pos : bedList)
        {
            IBlockState state = world.getBlockState(pos);
            state = state.getBlock().getExtendedState(state, world, pos);
            if (state.getBlock() instanceof BlockBed
                  && state.getValue(BlockBed.OCCUPIED)
                  && state.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.HEAD))
            {
                world.setBlockState(pos, state.withProperty(BlockBed.OCCUPIED, false), 0x03);
            }
        }
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CITIZEN;
    }

    @Override
    public void registerBlockPosition(@NotNull final IBlockState blockState, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(blockState, pos, world);

        BlockPos registrationPosition = pos;
        if (blockState.getBlock() instanceof BlockBed)
        {
            if (blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
            {
                registrationPosition = registrationPosition.offset(blockState.getValue(BlockBed.FACING));
            }

            if (!bedList.contains(registrationPosition))
            {
                bedList.add(registrationPosition);
            }
        }
    }

    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        getAssignedCitizen().stream()
          .filter(Objects::nonNull)
          .forEach(citizen -> citizen.setHomeBuilding(null));
    }

    @Override
    public void removeCitizen(@NotNull final ICitizenData citizen)
    {
        if (isCitizenAssigned(citizen))
        {
            super.removeCitizen(citizen);
            citizen.setHomeBuilding(null);

            femalePresent = false;
            malePresent = false;

            for (final ICitizenData citizenData : getAssignedCitizen())
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
                    return;
                }
            }
        }
    }

    /**
     * Updates the child creation timer and tries to assign homeless citizens on colony tick.
     *
     * @param colony the colony which ticks.
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (childCreationTimer > childCreationInterval)
        {
            childCreationTimer = 0;
            trySpawnChild();
        }
        childCreationTimer+= TWENTYFIVESEC;

        if (getAssignedCitizen().size() < getMaxInhabitants() && getColony() != null && !getColony().isManualHousing())
        {
            // 'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    /**
     * Try to spawn a new citizen as child.
     * Mom / dad entities are required and chosen randomly in this hut.
     * Childs inherit stats from their parents, avergaged +-2
     * Childs get assigned to a free housing slot in the colony to be raised there,
     * if the house has an adult living there the child takes its name and gets raised by it.
     */
    public void trySpawnChild()
    {
        // Spawn a child when adults are present
        if (colony.canMoveIn() && femalePresent && malePresent && colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getMaxCitizens())
        {
            ICitizenData mom = null;
            ICitizenData dad = null;

            for (final ICitizenData data : getAssignedCitizen())
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

            final ICitizenData newCitizen = colony.getCitizenManager().createAndRegisterNewCitizenData();

            final Random rand = new Random();

            // Inheriting stats from parents + some randomness. Capped by happiness in CitizenData setters.
            final int str = (mom.getStrength() + dad.getStrength()) / 2 + rand.nextInt(CHILD_STATS_VARIANCE) - rand.nextInt(CHILD_STATS_VARIANCE);
            final int cha = (mom.getCharisma() + dad.getCharisma()) / 2 + rand.nextInt(CHILD_STATS_VARIANCE) - rand.nextInt(CHILD_STATS_VARIANCE);
            final int dex = (mom.getDexterity() + dad.getDexterity()) / 2 + rand.nextInt(CHILD_STATS_VARIANCE) - rand.nextInt(CHILD_STATS_VARIANCE);
            final int end = (mom.getEndurance() + dad.getEndurance()) / 2 + rand.nextInt(CHILD_STATS_VARIANCE) - rand.nextInt(CHILD_STATS_VARIANCE);
            final int intelligence = (mom.getIntelligence() + dad.getIntelligence()) / 2 + rand.nextInt(CHILD_STATS_VARIANCE) - rand.nextInt(CHILD_STATS_VARIANCE);

            newCitizen.setIsChild(true);
            newCitizen.setStrength(str);
            newCitizen.setCharisma(cha);
            newCitizen.setDexterity(dex);
            newCitizen.setEndurance(end);
            newCitizen.setIntelligence(intelligence);

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
                for (final IBuilding build : colony.getBuildingManager().getBuildings().values())
                {
                    if (!(build instanceof BuildingHome))
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
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), "com.minecolonies.coremod.progress.newChild");
            colony.getCitizenManager().spawnOrCreateCitizen(newCitizen, colony.getWorld(), this.getPosition());
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

        String combinedName = "";
        for (final String namePart : newName)
        {
            combinedName = combinedName + namePart + " ";
        }
        child.setName(combinedName.trim());
    }

    @Override
    public int getMaxInhabitants()
    {
        return getBuildingLevel();
    }

    /**
     * Looks for a homeless citizen to add to the current building Calls.
     * {@link #assignCitizen(ICitizenData)}
     */
    private void addHomelessCitizens()
    {
        // Priotize missing genders for assigning
        for (@NotNull final ICitizenData citizen : getColony().getCitizenManager().getCitizens())
        {
            if (isFull())
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

        for (@NotNull final ICitizenData citizen : getColony().getCitizenManager().getCitizens())
        {
            if (isFull())
            {
                break;
            }
            moveCitizenToHut(citizen);
        }
    }

    /**
     * Moves the citizen into his new hut
     */
    private void moveCitizenToHut(final ICitizenData citizen)
    {
        // Move the citizen to a better hut
        if (citizen.getHomeBuilding() instanceof BuildingHome && citizen.getHomeBuilding().getBuildingLevel() < this.getBuildingLevel())
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
        if (!super.assignCitizen(citizen))
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

        citizen.setHomeBuilding(this);
        return true;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == NUM_ACHIEVEMENT_FIRST)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingColonist);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeColonistMax);
        }

        for (final Optional<AbstractEntityCitizen> entityCitizen : getAssignedEntities())
        {
            if (entityCitizen.isPresent() && entityCitizen.get().getCitizenJobHandler().getColonyJob() == null)
            {
                entityCitizen.get().getCitizenJobHandler().setModelDependingOnJob(null);
            }
        }
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.home;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(this.getAssignedCitizen().size());
        for (@NotNull final ICitizenData citizen : this.getAssignedCitizen())
        {
            buf.writeInt(citizen.getId());
        }
    }

    @Override
    public void setBuildingLevel(final int level)
    {
        super.setBuildingLevel(level);
        getColony().getCitizenManager().calculateMaxCitizens();
    }

    @NotNull
    public List<BlockPos> getBedList()
    {
        return new ArrayList<>(bedList);
    }

    /**
     * Return the child creation interval
     */
    public int getCitizenCreationInterval()
    {
        return childCreationInterval;
    }

    /**
     * Sets the child creation interval in seconds
     */
    public void setCitizenCreationInterval(final int childCreationInterval)
    {
        this.childCreationTimer = childCreationInterval;
    }

    /**
     * The view of the citizen hut.
     */
    public static class View extends AbstractBuildingView
    {
        @NotNull
        private final List<Integer> residents = new ArrayList<>();

        /**
         * Creates an instance of the citizen hut window.
         *
         * @param c the colonyView.
         * @param l the position the hut is at.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Getter for the list of residents.
         *
         * @return an unmodifiable list.
         */
        @NotNull
        public List<Integer> getResidents()
        {
            return Collections.unmodifiableList(residents);
        }

        /**
         * Removes a resident from the building.
         *
         * @param index the index to remove it from.
         */
        public void removeResident(final int index)
        {
            residents.remove(index);
        }

        /**
         * Add a resident from the building.
         *
         * @param id the id of the citizen.
         */
        public void addResident(final int id)
        {
            residents.add(id);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutCitizen(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);

            final int numResidents = buf.readInt();
            for (int i = 0; i < numResidents; ++i)
            {
                residents.add(buf.readInt());
            }
        }
    }
}
