package com.minecolonies.api.colony.requestsystem.requestable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Stack based requests interface for display purposes.
 */
public interface IStackBasedTask
{
    /**
     * Get the stack associated to the task.
     * @return the stack.
     */
    ItemStack getTaskStack();

    /**
     * Get the request related count.
     * @return the count.
     */
    int getDisplayCount();

    /**
     * Get a display prefix component.
     * @return the component.
     */
    MutableComponent getDisplayPrefix();
}
