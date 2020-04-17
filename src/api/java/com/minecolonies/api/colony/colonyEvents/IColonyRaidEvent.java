package com.minecolonies.api.colony.colonyEvents;

/**
 * Interface type for raid events
 */
public interface IColonyRaidEvent extends IColonyEntitySpawnEvent
{
    /**
     * Set that a citizen was killed in a raid.
     */
    void setKilledCitizenInRaid();
}
