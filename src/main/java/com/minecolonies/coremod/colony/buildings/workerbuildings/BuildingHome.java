package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHomeBuilding;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.ColonyConstants.NUM_ACHIEVEMENT_FIRST;
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
     * Instantiates a new citizen hut.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingHome(final Colony c, final BlockPos l)
    {
        super(c, l);
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
            if(!bedList.contains(bedPos))
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
            state = state.getBlock().getActualState(state, world, pos);
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
        }
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (getAssignedCitizen().size() < getMaxInhabitants() && getColony() != null && !getColony().isManualHousing())
        {
            // 'Capture' as many citizens into this house as possible
            addHomelessCitizens();
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
        for (@NotNull final CitizenData citizen : getColony().getCitizenManager().getCitizens())
        {
            // Move the citizen to a better hut
            if (citizen.getHomeBuilding() instanceof BuildingHome && citizen.getHomeBuilding().getBuildingLevel() < this.getBuildingLevel())
            {
                citizen.getHomeBuilding().removeCitizen(citizen);
            }
            if (citizen.getHomeBuilding() == null)
            {
                assignCitizen(citizen);

                if (isFull())
                {
                    break;
                }
            }
        }
    }

    @Override
    public boolean assignCitizen(final CitizenData citizen)
    {
        if (!super.assignCitizen(citizen))
        {
            return false;
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
            return new WindowHomeBuilding(this);
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
