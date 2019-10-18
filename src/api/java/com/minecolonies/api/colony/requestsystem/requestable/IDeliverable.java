package com.minecolonies.api.colony.requestsystem.requestable;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IDeliverable} is an Requestable that can be delivered.
 */
public interface IDeliverable extends IRetryable
{

    /**
     * Method called to check if a given stack matches this deliverable.
     * The first stack that returns true from this method is returned as a Deliverable.
     *
     * @param stack The stack to test.
     * @return true when the stack matches. False when not.
     */
    boolean matches(@NotNull final ItemStack stack);

    /**
     * Method called to get the amount of items that need to be in the stack.
     *
     * @return The amount of items that
     */
    int getCount();

    /**
     * Method to get the result of the delivery.
     *
     * @return The result of the delivery.
     */
    @NotNull
    ItemStack getResult();

    /**
     * Method to set the result of a delivery.
     *
     * @param result The result of the delivery.
     */
    void setResult(@NotNull final ItemStack result);

    /**
     * Creates a new instance of this requestable with the given count.
     *
     * @param newCount The new requestable, with the requested count.
     */
    IDeliverable copyWithCount(@NotNull final int newCount);
}
