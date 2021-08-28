package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;

/**
 * Interface type for raid events
 */
public interface IColonyRaidEvent extends IColonyEntitySpawnEvent
{
    /**
     * Get the normal raider type.
     *
     * @return the normal type.
     */
    EntityType<?> getNormalRaiderType();

    /**
     * Get the archer raider type.
     *
     * @return the archer type.
     */
    EntityType<?> getArcherRaiderType();

    /**
     * Get the boss raider type.
     *
     * @return the boss type.
     */
    EntityType<?> getBossRaiderType();

    /**
     * Add a spawner to an event.
     *
     * @param pos the pos to add the spawner at.
     */
    void addSpawner(final BlockPos pos);
}
