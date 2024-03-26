package com.minecolonies.api.colony.expeditions;

/**
 * Enum describing the different statuses of an expedition.
 */
public enum ExpeditionStatus
{
    /**
     * Default state, the moment this expedition is registered as an event to the colony they will embark.
     */
    CREATED(ExpeditionStatusType.ONGOING),
    /**
     * The expedition embarked on their journey and is currently in progress.
     */
    ONGOING(ExpeditionStatusType.ONGOING),
    /**
     * The expedition has returned safely to the colony.
     */
    RETURNED(ExpeditionStatusType.SUCCESSFUL),
    /**
     * The expedition has been killed off.
     */
    KILLED(ExpeditionStatusType.UNSUCCESSFUL),
    /**
     * The expedition has gotten lost.
     */
    LOST(ExpeditionStatusType.UNSUCCESSFUL);

    /**
     * The status type for this status.
     */
    private final ExpeditionStatusType statusType;

    /**
     * Internal constructor.
     */
    ExpeditionStatus(final ExpeditionStatusType statusType)
    {
        this.statusType = statusType;
    }

    /**
     * Get the status type for this status.
     *
     * @return the status type enum.
     */
    public ExpeditionStatusType getStatusType()
    {
        return statusType;
    }
}