package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.util.math.BlockPos;

/**
 * An colony event which spawns at a certain position
 */
public interface IColonySpawnEvent extends IColonyEvent
{
    /**
     * Sets the spawn point
     *
     * @param spawnPoint the spawn point to set.
     */
    void setSpawnPoint(BlockPos spawnPoint);

    /**
     * The position the event starts at
     *
     * @return the spawn pos.
     */
    BlockPos getSpawnPos();
}
