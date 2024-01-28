package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Predicate;

/**
 * Module type to register specific blocks to a building (beds, workstations, etc).
 */
public interface IAltersRequiredItems extends IBuildingModule
{
    /**
     * Check if additional items have to be kept and add to map if necessary.
     * @param consumer consumer that adds items to it.
     */
    void alterItemsToBeKept(final TriConsumer<Predicate<ItemStack>, Integer, Boolean> consumer);
}
