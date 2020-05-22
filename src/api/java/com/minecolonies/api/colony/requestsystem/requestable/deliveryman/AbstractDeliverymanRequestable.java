package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

public abstract class AbstractDeliverymanRequestable implements IDeliverymanRequestable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    protected static final String NBT_PRIORITY = "Priority";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    public static final int MAX_DELIVERYMAN_STANDARD_PRIORITY = 10;
    public static final int MAX_DELIVERYMAN_AGING_PRIORITY    = 11;
    public static final int MAX_DELIVERYMAN_PLAYER_PRIORITY   = 12;

    protected int priority = 0;

    protected AbstractDeliverymanRequestable(final int priority)
    {
        this.priority = priority;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void incrementPriorityDueToAging()
    {
        // The priority set by by the aging mechanism can actually exceed the maximum priority that requesters can choose.
        // Worst case, the priority queue turns into a FIFO queue for really old requests, with new maximum-priority requests having to wait.
        priority = Math.min(MAX_DELIVERYMAN_AGING_PRIORITY, priority + 1);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractDeliverymanRequestable))
        {
            return false;
        }
        return getPriority() == ((AbstractDeliverymanRequestable) o).getPriority();
    }

    @Override
    public int hashCode()
    {
        return getPriority();
    }
}
