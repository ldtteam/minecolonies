package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

public abstract class AbstractDeliverymanRequestable implements IDeliverymanRequestable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    protected static final String NBT_PRIORITY = "Priority";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private static final int MAX_PRIORITY = 10;

    protected int priority = 0;

    protected AbstractDeliverymanRequestable(final int priority)
    {
        this.priority = Math.min(MAX_PRIORITY, priority);
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void incrementPriorityDueToAging()
    {
        priority = Math.min(MAX_PRIORITY, priority + 1);
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
