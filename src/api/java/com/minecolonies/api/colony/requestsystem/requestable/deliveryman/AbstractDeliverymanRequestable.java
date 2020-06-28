package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

/**
 * Abstract class for all deliveryman-requests
 */
public abstract class AbstractDeliverymanRequestable implements IDeliverymanRequestable
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    protected static final String NBT_PRIORITY = "Priority";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private static final int MAX_BUILDING_PRIORITY     = 10;
    private static final int DEFAULT_DELIVERY_PRIORITY = 13;
    private static final int MAX_AGING_PRIORITY        = 14;
    private static final int PLAYER_ACTION_PRIORITY    = 15;

    protected int priority = 0;

    /**
     * Constructor for deliveryman requestables
     *
     * @param priority The priority of the request. Higher priority -> Earlier delivery/pickup
     */
    protected AbstractDeliverymanRequestable(final int priority)
    {
        this.priority = priority;
    }

    /**
     * Scales the priority to the desired internal value.
     * This is used so that the actual priorities are not just 1-10, but i.e. 1-100 (x^2)
     * This will effectively make the aging-algorithm, which always increments by 1, slower.
     * The function can be anything - a linear scaler, quadratic, exponential, whatever.
     * Adapt over time to find the best solution.
     */
    public static int scaledPriority(final int priority)
    {
        // This version makes the increase quadratic
        // return (int) Math.pow(priority, 2);

        return priority;
    }

    /**
     * Gets the maximum priority allowed to be set in the building GUI.
     * This is the "normal" setting available to players.
     *
     * @param returnScaled true if the value should be returned scaled
     * @return the scaled/unscaled priority
     */
    public static int getMaxBuildingPriority(final boolean returnScaled)
    {
        return returnScaled ? scaledPriority(MAX_BUILDING_PRIORITY) : MAX_BUILDING_PRIORITY;
    }

    /**
     * Gets the priority given to deliveries.
     * This affects follow-up deliveries from crafters, and deliveries from the warehouse.
     *
     * @param returnScaled true if the value should be returned scaled
     * @return the scaled/unscaled priority
     */
    public static int getDefaultDeliveryPriority(final boolean returnScaled)
    {
        return returnScaled ? scaledPriority(DEFAULT_DELIVERY_PRIORITY) : DEFAULT_DELIVERY_PRIORITY;
    }

    /**
     * Gets the maximum priority the aging mechanism can assign.
     * After that, priorities can not naturally increase.
     *
     * @param returnScaled true if the value should be returned scaled
     * @return the scaled/unscaled priority
     */
    public static int getMaxAgingPriority(final boolean returnScaled)
    {
        return returnScaled ? scaledPriority(MAX_AGING_PRIORITY) : MAX_AGING_PRIORITY;
    }

    /**
     * Gets the priority given to the Request-Pickup-Now-feature
     * TODO: Eventually, this should also affect the Postbox.
     *
     * @param returnScaled true if the value should be returned scaled
     * @return the scaled/unscaled priority
     */
    public static int getPlayerActionPriority(final boolean returnScaled)
    {
        return returnScaled ? scaledPriority(PLAYER_ACTION_PRIORITY) : PLAYER_ACTION_PRIORITY;
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
        priority = Math.min(getMaxAgingPriority(true), priority + 1);
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
