package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.requestsystem.requestable.Food;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHomeBuilding;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * The class of the citizen hut.
 */
public class BuildingHome extends AbstractBuildingHut
{
    /**
     * The tag used to store the residents.
     */
    private static final String TAG_RESIDENTS = "residents";

    /**
     * List storing all beds which have been registered to the building.
     */
    private static final String TAG_BEDS = "beds";

    /**
     * Constant determining how big of food request to start.
     */
    private static final int CONST_FOOD_REQUEST_SIZE = 5;

    /**
     * The string describing the hut.
     */
    private static final String CITIZEN = "Citizen";

    /**
     * List of all citizen.
     */
    @NotNull
    private final List<CitizenData> residents = new ArrayList<>();

    /**
     * List of all bedList.
     */
    @NotNull
    private final List<BlockPos> bedList = new ArrayList<>();

    /**
     * Boolean value describing if any citizen needs food.
     */
    private boolean isFoodNeeded = false;

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

        residents.clear();

        final int[] residentIds = compound.getIntArray(TAG_RESIDENTS);
        for (final int citizenId : residentIds)
        {
            final CitizenData citizen = getColony().getCitizen(citizenId);
            if (citizen != null)
            {
                // Bypass addResident (which marks dirty)
                residents.add(citizen);
                citizen.setHomeBuilding(this);
            }
        }
        final NBTTagList bedTagList = compound.getTagList(TAG_BEDS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < bedTagList.tagCount(); ++i)
        {
            final NBTTagCompound bedCompound = bedTagList.getCompoundTagAt(i);
            bedList.add(NBTUtil.getPosFromTag(bedCompound));
        }
    }

    @NotNull
    public List<BlockPos> getBedList()
    {
        return new ArrayList<>(bedList);
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

        if (!residents.isEmpty())
        {
            @NotNull final int[] residentIds = new int[residents.size()];
            for (int i = 0; i < residents.size(); ++i)
            {
                residentIds[i] = residents.get(i).getId();
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
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block == Blocks.BED)
        {
            bedList.add(pos);
        }
    }

    @Override
    public void onDestroyed()
    {
        residents.stream()
                .filter(Objects::nonNull)
                .forEach(citizen -> citizen.setHomeBuilding(null));
        residents.clear();
        super.onDestroyed();
    }

    @Override
    public void removeCitizen(@NotNull final CitizenData citizen)
    {
        if (residents.contains(citizen))
        {
            citizen.setHomeBuilding(null);
            residents.remove(citizen);
            markDirty();
        }
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        if (residents.size() < getMaxInhabitants() && getColony() != null && !getColony().isManualHousing())
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
     * {@link #addResident(CitizenData)}
     */
    private void addHomelessCitizens()
    {
        for (@NotNull final CitizenData citizen : getColony().getCitizens().values())
        {
            // Move the citizen to a better hut
            if (citizen.getHomeBuilding() != null && citizen.getHomeBuilding().getBuildingLevel() < this.getBuildingLevel())
            {
                // The citizen can move to this hut to improve conditions
                citizen.getHomeBuilding().removeCitizen(citizen);
            }
            if (citizen.getHomeBuilding() == null)
            {
                addResident(citizen);

                if (isFull())
                {
                    break;
                }
            }
        }
    }

    /**
     * Checks if the building is full.
     *
     * @return true if so.
     */
    public boolean isFull()
    {
        return residents.size() >= getMaxInhabitants();
    }

    /**
     * Adds the citizen to the building.
     *
     * @param citizen Citizen to add.
     */
    public void addResident(@NotNull final CitizenData citizen)
    {
        residents.add(citizen);
        citizen.setHomeBuilding(this);

        markDirty();
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

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingColonist);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeColonistMax);
        }
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeInt(residents.size());
        for (@NotNull final CitizenData citizen : residents)
        {
            buf.writeInt(citizen.getId());
        }
    }

    @Override
    public void setBuildingLevel(final int level)
    {
        super.setBuildingLevel(level);
        getColony().calculateMaxCitizens();
    }

    /**
     * Returns whether the citizen has this as home or not.
     *
     * @param citizen Citizen to check.
     * @return True if citizen lives here, otherwise false.
     */
    public boolean hasResident(final CitizenData citizen)
    {
        return residents.contains(citizen);
    }

    /**
     * Checks if food in the home is required.
     * If yes set foodNeeded to true, else to false.
     */
    public void checkIfFoodNeeded()
    {
        residents.stream()
          .filter(resident -> resident.getSaturation() < EntityCitizen.HIGH_SATURATION)
          .filter(resident -> !hasWorkerOpenRequestsOfType(resident, Food.class))
          .forEach(resident -> {
              Food foodRequest = new Food(CONST_FOOD_REQUEST_SIZE);
              createRequest(resident, foodRequest);
          });
    }

    /**
     * The view of the citizen hut.
     */
    public static class View extends AbstractBuildingHut.View
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
