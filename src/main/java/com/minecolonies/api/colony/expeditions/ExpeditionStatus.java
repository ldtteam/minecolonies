package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.colonyEvents.EventStatus;

/**
 * Enum describing the different statuses of an expedition.
 */
public enum ExpeditionStatus
{
    /**
     * Default state, the moment this expedition is registered as an event to the colony they will embark.
     */
    CREATED(EventStatus.STARTING, ExpeditionStatusType.ONGOING),
    /**
     * The expedition embarked on their journey and is currently in progress.
     */
    EMBARKED(EventStatus.PROGRESSING, ExpeditionStatusType.ONGOING),
    /**
     * The expedition has returned safely to the colony.
     */
    RETURNED(EventStatus.DONE, ExpeditionStatusType.SUCCESSFUL),
    /**
     * The expedition has been killed off.
     */
    KILLED(EventStatus.DONE, ExpeditionStatusType.UNSUCCESSFUL),
    /**
     * The expedition has gotten lost.
     */
    LOST(EventStatus.DONE, ExpeditionStatusType.UNSUCCESSFUL);

    /**
     * The underlying status for the expedition event.
     */
    private final EventStatus eventStatus;

    /**
     * The status type for this status.
     */
    private final ExpeditionStatusType statusType;

    /**
     * Internal constructor.
     */
    ExpeditionStatus(final EventStatus eventStatus, final ExpeditionStatusType statusType)
    {
        this.eventStatus = eventStatus;
        this.statusType = statusType;
    }

    /**
     * Get the underlying status for the expedition event.
     *
     * @return the event status.
     */
    public EventStatus getEventStatus()
    {
        return eventStatus;
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