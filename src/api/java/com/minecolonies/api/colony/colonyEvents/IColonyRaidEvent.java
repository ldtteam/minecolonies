package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;

import java.util.List;

/**
 * Interface type for raid events
 */
public interface IColonyRaidEvent extends IColonyEvent, IColonySpawnEvent, IColonyEntitySpawnEvent
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

    /**
     * Gets the list of waypoints
     */
    List<BlockPos> getWayPoints();

    /**
     * Whether or not the raid is still active.
     * @return true if so.
     */
    default boolean isRaidActive()
    {
        return getStatus() == EventStatus.PROGRESSING ||getStatus() == EventStatus.PREPARING;
    }

    /**
     * Set the raid event to mercy.
     */
    void setMercyEnd();
}
