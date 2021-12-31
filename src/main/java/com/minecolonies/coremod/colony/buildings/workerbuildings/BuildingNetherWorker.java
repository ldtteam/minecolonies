package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;


import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

import java.util.List;
import java.util.Map;


public class BuildingNetherWorker extends AbstractBuilding
{
    /**
     * 
     * Constant name for the Netherworker building
     */
    private static final String NETHER_WORKER = "netherworker";

    /**
     * How many trips we've run this period
     */
    private static final String TAG_CURRENT_TRIPS = "current_trips";

    /**
     * Which day in the period is it? 
     */
    private static final String TAG_CURRENT_DAY = "current_day";

    /**
     * How many trips we can make per period by default
     */
    private static final int MAX_PER_PERIOD = 1;

    /**
     * How many days are in a period by default
     */
    private static final int PERIOD_DAYS = 1;

    /**
     * Which day we're at in the current period
     */
    private int currentPeriodDay = 0;

    /**
     * How many trips we've done in the current period
     */
    private int currentTrips = 0;

    /**
     * ServerTime for the last 'day' snapshot, to track days when doDaylightCycle is not happening.
     */
    private long snapTime;

    public BuildingNetherWorker(@NotNull IColony colony, BlockPos pos)
    {
        super(colony, pos);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SWORD, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));

        keepX.put(itemStack -> itemStack.getItem() instanceof FlintAndSteelItem, new Tuple<>(1, true));

        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof ArmorItem
                && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.HEAD, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof ArmorItem
                && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.CHEST, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof ArmorItem
                && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.LEGS, new Tuple<>(1, true));
        keepX.put(itemStack -> !ItemStackUtils.isEmpty(itemStack)
                && itemStack.getItem() instanceof ArmorItem
                && ((ArmorItem) itemStack.getItem()).getSlot() == EquipmentSlotType.FEET, new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return NETHER_WORKER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void onWakeUp()
    {
        super.onWakeUp();
        snapTime = colony.getWorld().getDayTime();
        if(this.currentPeriodDay < getPeriodDays())
        {
            this.currentPeriodDay++;
        }
        else
        {
            this.currentPeriodDay = 0;
            this.currentTrips = 0;
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        if(compound.contains(TAG_CURRENT_TRIPS))
        {
            this.currentTrips = compound.getInt(TAG_CURRENT_TRIPS);
        }

        if(compound.contains(TAG_CURRENT_DAY))
        {
            this.currentPeriodDay = compound.getInt(TAG_CURRENT_DAY);
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        compound.putInt(TAG_CURRENT_TRIPS, this.currentTrips);
        compound.putInt(TAG_CURRENT_DAY, this.currentPeriodDay);

        return compound;
    }

    /**
     * Check to see if it's valid to do a trip by checking how many done in this current period
     * @return true if the worker can go to the nether
     */
    public boolean canDoTrip()
    {
        if(snapTime == 0)
        {
            snapTime = colony.getWorld().getDayTime();
        }
        if(Math.abs(colony.getWorld().getDayTime() - snapTime) >= 24000)
        {
            //Make sure we're incrementing if day/night cycle isn't running. 
            this.currentPeriodDay++;
        }
        return this.currentTrips < getMaxPerPeriod();
    }

    /**
     * Let the building know we're doing a trip
     */
    public void recordTrip()
    {
        this.currentTrips++;
    }

    /**
     * Get the tagged location that the worker should walk to in the portal. 
     * This should be a 'air block' in the portal that can directly be checked to see if the portal is open
     * @return the block above the tag, null if not available
     */
    public BlockPos getPortalLocation()
    {
        if (tileEntity != null && !tileEntity.getPositionedTags().isEmpty())
        {
            for (final Map.Entry<BlockPos, List<String>> entry : tileEntity.getPositionedTags().entrySet())
            {
                if(entry.getValue().contains("portal"))
                {
                    //Tagged block for a portal location should be one of the obsidian block at the bottom of the opening
                    return getPosition().offset(entry.getKey().above());
                }
            }
        }
        return null;
    }

    /**
     * Get the max per period, potentially modified by research
     * @return
     */
    public static int getMaxPerPeriod()
    {
        return MAX_PER_PERIOD;
    }

    /**
     * Get how many days are in a period, potentially modified by research.
     * @return
     */
    public static int getPeriodDays()
    {
        return PERIOD_DAYS;
    }


    public static class CraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }
    }
}
