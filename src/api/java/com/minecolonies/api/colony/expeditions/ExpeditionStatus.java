package com.minecolonies.api.colony.expeditions;

/**
 * Enum describing the different statuses of an expedition.
 */
public enum ExpeditionStatus
{
    /**
     * Initial expedition state, expedition exists but has not been started yet.
     */
    CREATED,
    /**
     * The expedition embarked on their journey and is currently in progress.
     */
    EMBARKED,
    /**
     * The expedition has returned safely to the colony.
     */
    RETURNED,
    /**
     * The expedition has not returned in time, they either got lost or have been killed.
     */
    MISSING;
}