package com.minecolonies.api.colony.requestsystem.requestable;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * An {@link IConcreteDeliverable} is an Requestable that can be looked up fast in the warehouse, and delivered.
 */
public interface IConcreteDeliverable extends IDeliverable
{
    /**
     * Get a list of concrete items requested by this deliverable
     * @return
     */
    List<ItemStack> getRequestedItems();
}
