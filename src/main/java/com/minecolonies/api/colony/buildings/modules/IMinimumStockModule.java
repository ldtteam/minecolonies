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
     * If an incoming request is a minimum stock request.
     *
     * @param request the request to check.
     * @return true if so.
     */
    boolean isMinimumStockRequest(final IRequest<? extends IDeliverable> request);

    /**
     * Check if this stack is stocked.
     * @param stack the stack to check.
     * @return true if stocked.
     */
    boolean isStocked(ItemStack stack);
}
