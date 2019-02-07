package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
import static com.minecolonies.api.util.constant.ColonyConstants.ONWORLD_TICK_AVERAGE;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;
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
     * Interval at which the childen are created, in ticks.
     * Every 20 min it tries to spawn a child, 20min*60s*20ticks
     */
    private int childCreationInterval = 1200;

    /**
     * The timer counting ticks to the next time creating a child
     */
    private int childCreationTimer = 0;

    /**
     * Instantiates a new citizen hut.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingHome(final Colony c, final BlockPos l)
    {
        super(c, l);
        Random rand = new Random();
        childCreationTimer = rand.nextInt(childCreationInterval);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_RESIDENTS))
        {
            final int[] residentIds = compound.getIntArray(TAG_RESIDENTS);
            for (final int citizenId : residentIds)
            {
                final CitizenData citizen = getColony().getCitizenManager().getCitizen(citizenId);
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
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

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
    public void removeCitizen(@NotNull final CitizenData citizen)
    {
        if (isCitizenAssigned(citizen))
        {
            super.removeCitizen(citizen);
            citizen.setHomeBuilding(null);

            femalePresent = false;
            malePresent = false;

            for (CitizenData citizenData : getAssignedCitizen())
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

    @Override
    public void secondsWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (childCreationTimer > childCreationInterval)
        {
            childCreationTimer = 0;
            trySpawnChild();
        }
        childCreationTimer++;
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        //
        // Code below this check won't lag each tick anymore
        //
        if (!Colony.shallUpdate(event.world, ONWORLD_TICK_AVERAGE))
        {
            return;
        }

        if (getAssignedCitizen().size() < getMaxInhabitants() && getColony() != null && !getColony().isManualHousing())
        {
            // 'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    /**
     * Try to spawn a new citizen as child.
     */
    public void trySpawnChild()
    {
        // Spawn a child when adults are present and the house has space, so level 3+
        if (femalePresent && malePresent && colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getMaxCitizens())
        {
            CitizenData mom = null;
            CitizenData dad = null;

            for (CitizenData data : getAssignedCitizen())
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

            CitizenData newCitizen = colony.getCitizenManager().createAndRegisterNewCitizenData();

            Random rand = new Random();

            // Inheriting stats from parents + some randomness. Capped by happiness
            int str = (mom.getStrength() + dad.getStrength()) / 2 + rand.nextInt(3) - rand.nextInt(3);
            str = str > colony.getOverallHappiness() ? (int) colony.getOverallHappiness() : str;

            int cha = (mom.getCharisma() + dad.getCharisma()) / 2 + rand.nextInt(3) - rand.nextInt(3);
            cha = cha > colony.getOverallHappiness() ? (int) colony.getOverallHappiness() : cha;

            int dex = (mom.getDexterity() + dad.getDexterity()) / 2 + rand.nextInt(3) - rand.nextInt(3);
            dex = dex > colony.getOverallHappiness() ? (int) colony.getOverallHappiness() : dex;

            int end = (mom.getEndurance() + dad.getEndurance()) / 2 + rand.nextInt(3) - rand.nextInt(3);
            end = end > colony.getOverallHappiness() ? (int) colony.getOverallHappiness() : end;

            int intelligence = (mom.getIntelligence() + dad.getIntelligence()) / 2 + rand.nextInt(3) - rand.nextInt(3);
            intelligence = intelligence > colony.getOverallHappiness() ? (int) colony.getOverallHappiness() : intelligence;

            newCitizen.setIsChild(true);
            newCitizen.setStrength(str);
            newCitizen.setCharisma(cha);
            newCitizen.setDexterity(dex);
            newCitizen.setEndurance(end);
            newCitizen.setIntelligence(intelligence);

            // Assign citizen to a house
            if (assignCitizen(newCitizen))
            {
                // New citizen name for this hut
                String parentName;
                if (rand.nextInt(2) == 1)
                {
                    parentName = mom.getName().split(" ")[2];
                }
                else
                {
                    parentName = dad.getName().split(" ")[2];
                }

                String[] newName = newCitizen.getName().split(" ");
                newName[2] = parentName;
                newCitizen.setName(newName[0] + " " + newName[1] + " " + newName[2]);
                Log.getLogger().info("newcitizen name:" + newCitizen.getName());
            }
            else
            {
                // Assign to a different citizen hut and adopt
                for (final AbstractBuilding build : colony.getBuildingManager().getBuildings().values())
                {
                    if (!(build instanceof BuildingHome))
                    {
                        continue;
                    }

                    // Try assigning
                    if (build.assignCitizen(newCitizen))
                    {
                        // Get Parent Name
                        for (CitizenData data : build.getAssignedCitizen())
                        {
                            // Exclude child itself
                            if (data.getId() != newCitizen.getId())
                            {
                                String[] newName = newCitizen.getName().split(" ");
                                newName[2] = data.getName().split(" ")[2];
                                newCitizen.setName(newName[0] + " " + newName[1] + " " + newName[2]);
                                Log.getLogger().info("newcitizen name:" + newCitizen.getName());
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            colony.getCitizenManager().spawnOrCreateCitizen(newCitizen, colony.getWorld(), this.getLocation());
        }
    }

    @Override
    public int getMaxInhabitants()
    {
        return getBuildingLevel();
    }

    /**
     * Looks for a homeless citizen to add to the current building Calls.
     * {@link #assignCitizen(CitizenData)}
     */
    private void addHomelessCitizens()
    {
        // Priotize missing genders for assigning
        for (@NotNull final CitizenData citizen : getColony().getCitizenManager().getCitizens())
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

        for (@NotNull final CitizenData citizen : getColony().getCitizenManager().getCitizens())
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
    private void moveCitizenToHut(final CitizenData citizen)
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
    public boolean assignCitizen(final CitizenData citizen)
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

        for (final Optional<EntityCitizen> entityCitizen : getAssignedEntities())
        {
            if (entityCitizen.isPresent() && entityCitizen.get().getCitizenJobHandler().getColonyJob() == null)
            {
                entityCitizen.get().getCitizenJobHandler().setModelDependingOnJob(null);
            }
        }
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(this.getAssignedCitizen().size());
        for (@NotNull final CitizenData citizen : this.getAssignedCitizen())
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
     * Sets the child creation interval
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
        public View(final ColonyView c, final BlockPos l)
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
