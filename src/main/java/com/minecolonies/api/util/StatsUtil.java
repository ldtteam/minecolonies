package com.minecolonies.api.util;

import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

import java.util.Map;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.modules.BuildingStatisticsModule;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

/**
 * A variety of helper functions to facilitate statistics collection by buildings.
 */
public class StatsUtil 
{
    /**
     * Safely return the name of the item in the given slot of the given furnace for use in statistics.
     * Returns empty if no furnace, or no item in the slot.
     *
     * @param furnace the furnace to get the item from.
     * @param slot    the slot to get the item from.
     * @return the name of the item.
     */
    public static String nameForStats(final FurnaceBlockEntity furnace, final int slot)
    {
        String name = "";

        if (furnace != null) 
        {
            ItemStack item = furnace.getItem(slot);
            if (item != null) 
            {
                name = item.getDescriptionId();
            }
        }

        return name;
    }

    /**
     * Track a stat for a given building using the standard STATS_MODULE, with some null safety built in.
     * Takes a map of ItemStacks and the total amounts of those stacks to be added to the stats and calls 
     * overridden individual trackStat methods on each of them.
     * @param building the building to track the stat for.
     * @param statName the identifier for the stat.
     * @param itemMap the items to track the stats for.
     */
    public static void trackStatByStackMap(IBuilding building, String statName, Object2IntMap<ItemStack> itemMap)
    {
        for (Object2IntMap.Entry<ItemStack> entry : itemMap.object2IntEntrySet())
        {
            ItemStack stack = entry.getKey();
            int count = entry.getIntValue();
            trackStatByStack(building, statName, stack, count);
        }
    }

    /**
     * Track a stat for a given building using the standard STATS_MODULE, with some null safety built in.
     * @param building the building to track the stat for.
     * @param statIdentifier the identifier for the stat.
     * @param stack the ItemStack to track the stat for (displayName will be read from the descriptionId)
     * @param count the number of the item to track the stat for.
     */
    public static void trackStatByStack(IBuilding building, String statIdentifier, ItemStack stack, int count) 
    {
        if (stack == null) 
        {
            Log.getLogger().warn("Attempted to track stat '{}' with null stack: ", statIdentifier);
            return;
        }

        trackStat(building, statIdentifier, stack.getDescriptionId(), count);
    }

    /**
     * Track a stat for a given building using the standard STATS_MODULE, with some null safety built in.
     * @param building the building to track the stat for.
     * @param statIdentifier the identifier for the stat.
     * @param displayName the display name of the item to track the stat for.
     * @param count the number of the item to track the stat for.
     */
    public static void trackStat(IBuilding building, String statIdentifier, String displayName, int count) 
    {
        if (building == null) 
        {
            Log.getLogger().warn("Attempted to track stat '{}' with null building: ", statIdentifier);
            return;
        }

        String statKey = statIdentifier + ";" + displayName;
        BuildingStatisticsModule statsModule = building.getModule(STATS_MODULE);
        
        if (statsModule != null) 
        {
            statsModule.incrementBy(statKey, count);
        } else {
            Log.getLogger().error("Attempt to track stats on a building that has no statistics module: {}", building);
        }
    }

    /**
     * Track a stat for a given building using the standard STATS_MODULE, with some null safety built in.
     * @param building the building to track the stat for.
     * @param statIdentifier the identifier for the stat.
     * @param displayName the display name of the item to track the stat for, as a Component.
     * @param count the number of the item to track the stat for.
     */
    public static void trackStat(IBuilding building, String statIdentifier, Component displayName, int count) 
    {
        if (displayName == null) 
        {
            Log.getLogger().warn("Attempted to track stat '{}' with null displayName as component: ", statIdentifier);
            return;
        }

        trackStat(building, statIdentifier, displayName.getString(), count);
    }

}
