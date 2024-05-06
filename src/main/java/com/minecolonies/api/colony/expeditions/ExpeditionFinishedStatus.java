package com.minecolonies.api.colony.expeditions;

/**
 * Enum describing the different finished statuses of an expedition.
 */
public enum ExpeditionFinishedStatus
{
    /**
     * The expedition has returned safely to the colony.
     */
    RETURNED(ExpeditionFinishedStatusType.SUCCESSFUL),
    /**
     * The expedition has been killed off.
     */
    KILLED(ExpeditionFinishedStatusType.UNSUCCESSFUL),
    /**
     * The expedition has gotten lost.
     */
    LOST(ExpeditionFinishedStatusType.UNSUCCESSFUL);

    /**
     * The status type for this status.
     */
    private final ExpeditionFinishedStatusType statusType;

    /**
     * Internal constructor.
     */
    ExpeditionFinishedStatus(final ExpeditionFinishedStatusType statusType)
    {
        this.statusType = statusType;
    }

    /**
     * Get the status type for this status.
     *
     * @return the status type enum.
     */
    public ExpeditionFinishedStatusType getStatusType()
    {
        return statusType;
    }
}