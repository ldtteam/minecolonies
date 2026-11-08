package com.minecolonies.core.network;

import net.minecraft.core.BlockPos;

/**
 * Interface for range restricted packet interactions
 *
 * @param <T>
 */
public interface IRangeRestrictedPacket
{
    /**
     * Default allowed range
     */
    static int DEFAULT_RANGE = 15;

    /**
     * Range check to implement
     */
    default boolean isInRange(BlockPos from, BlockPos to)
    {
        return from.distSqr(to) <= DEFAULT_RANGE * DEFAULT_RANGE;
    }

    /**
     * Max allowed range in blocks
     *
     * @return
     */
    default int getMaxRange()
    {
        return DEFAULT_RANGE;
    }
}
