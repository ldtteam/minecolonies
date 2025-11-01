package com.minecolonies.core.entity.pathfinding.pathjobs;

import net.minecraft.core.BlockPos;

/**
 * Interface for path jobs with a destination/desired direction
 */
public interface IDestinationPathJob
{
    /**
     * Return the destination
     *
     * @return destination
     */
    public BlockPos getDestination();
}
