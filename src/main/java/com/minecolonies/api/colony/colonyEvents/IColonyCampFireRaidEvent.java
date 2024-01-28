package com.minecolonies.api.colony.colonyEvents;

/**
 * Raid event with campfires for delayed start
 */
public interface IColonyCampFireRaidEvent
{
    /**
     * Sets the intervals to wait before moving away from campfires.
     *
     * @param time in MAX_TICK(25s) intervals
     */
    void setCampFireTime(int time);
}
