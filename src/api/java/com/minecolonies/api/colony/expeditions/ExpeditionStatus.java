package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.colonyEvents.EventStatus;

/**
 * Enum describing the different statuses of an expedition.
 */
public enum ExpeditionStatus
{
    /**
     * Initial expedition state, expedition exists but has not been started yet.
     */
    CREATED(EventStatus.WAITING),
    /**
     * The expedition is ready and is about to leave.
     */
    READY(EventStatus.STARTING),
    /**
     * The expedition embarked on their journey and is currently in progress.
     */
    EMBARKED(EventStatus.PROGRESSING),
    /**
     * The expedition has returned safely to the colony.
     */
    RETURNED(EventStatus.DONE),
    /**
     * The expedition has been killed off.
     */
    KILLED(EventStatus.DONE),
    /**
     * The expedition has gotten lost.
     */
    LOST(EventStatus.DONE);

    /**
     * The underlying status for the expedition event.
     */
    private final EventStatus eventStatus;

    /**
     * Internal constructor.
     */
    ExpeditionStatus(final EventStatus eventStatus)
    {
        this.eventStatus = eventStatus;
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
}