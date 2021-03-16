package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Module type to register specific blocks to a building (beds, workstations, etc).
 */
public interface IAltersRequiredItems extends IBuildingModule
{
    /**
     * Check if additional items have to be kept and add to map if necessary.
     * @param toKeep the map of items that should be kept already.
     */
    void alterItemsToBeKept(final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep);
}
