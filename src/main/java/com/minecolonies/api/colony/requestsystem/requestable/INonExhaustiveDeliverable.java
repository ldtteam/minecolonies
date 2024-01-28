package com.minecolonies.api.colony.requestsystem.requestable;

/**
 * An {@link INonExhaustiveDeliverable} is an Requestable that can be looked up fast in the warehouse, and delivered.
 */
public interface INonExhaustiveDeliverable extends IDeliverable
{
    /**
     * Get the amount that is supposed to be kept left over at the warehouse.
     * @return the amount.
     */
    int getLeftOver();
}
