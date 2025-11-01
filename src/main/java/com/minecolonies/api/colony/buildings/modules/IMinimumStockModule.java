package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import net.minecraft.world.item.ItemStack;

/**
 * Module for adding minimum stocks to buildings.
 */
public interface IMinimumStockModule extends IBuildingModule
{
    /**
     * Remove the minimum stock.
     *
     * @param itemStack the stack to remove.
     */
    void removeMinimumStock(final ItemStack itemStack);

    /**
     * Add the minimum stock of the warehouse to this building.
     *
     * @param itemStack the itemStack to add.
     * @param quantity  the quantity.
     */
    void addMinimumStock(final ItemStack itemStack, final int quantity);

    /**
     * Check if this stack is stocked.
     * @param stack the stack to check.
     * @return true if stocked.
     */
    boolean isStocked(ItemStack stack);
}
