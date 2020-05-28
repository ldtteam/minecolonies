package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;

/**
 * Marker interface for requestables handled by deliverymen.
 */
public interface IDeliverymanRequestable extends IRequestable
{
    /**
     * Returns the priority of the Requestable.
     * The higher the priority, the more urgent it is and the faster a deliveryman will handle it.
     *
     * @return The current priority of the request
     */
    int getPriority();

    /**
     * This will increment the priority due to the aging algorithm.
     * This is important because it prevents starvation, making older requests successively more important.
     */
    void incrementPriorityDueToAging();
}
