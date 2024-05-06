package com.minecolonies.api.colony.expeditions;

/**
 * Enum describing the different statuses of an expedition.
 */
public enum ExpeditionStatus
{
    /**
     * The expedition has been created but not accepted yet.
     */
    CREATED,
    /**
     * The expedition has been accepted but has not been sent away just yet.
     */
    ACCEPTED,
    /**
     * The expedition is currently active.
     */
    ONGOING,
    /**
     * The expedition is finished.
     */
    FINISHED,
    /**
     * The expedition does not exist.
     */
    UNKNOWN
}