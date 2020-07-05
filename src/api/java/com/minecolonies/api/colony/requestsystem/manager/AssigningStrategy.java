package com.minecolonies.api.colony.requestsystem.manager;

/**
 * Enum determining the assigning strategy.
 */
public enum AssigningStrategy
{
    /**
     * Default strategy.
     */
    PRIORITY_BASED,

    /**
     * Strategy that will ensure that the request depth is small.
     */
    FASTEST_FIRST
}
