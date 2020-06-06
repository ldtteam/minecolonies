package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.entity.EntityType;

/**
 * Interface type for raid events
 */
public interface IColonyRaidEvent extends IColonyEntitySpawnEvent
{
    /**
     * Set that a citizen was killed in a raid.
     */
    void setKilledCitizenInRaid();

    /**
     * Get the normal raider type.
     * @return the normal type.
     */
    EntityType<?> getNormalRaiderType();

    /**
     * Get the archer raider type.
     * @return the archer type.
     */
    EntityType<?> getArcherRaiderType();

    /**
     * Get the boss raider type.
     * @return the boss type.
     */
    EntityType<?> getBossRaiderType();
}
