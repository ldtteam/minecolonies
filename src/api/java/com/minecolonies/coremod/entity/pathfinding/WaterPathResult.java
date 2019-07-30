package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.util.math.BlockPos;

/**
 * Contains the result of the path job to find water.
 */
public class WaterPathResult extends PathResult
{
    /**
     * The position of the pond.
     */
    public BlockPos pond;

    /**
     * If the pond is empty.
     */
    public boolean isEmpty;
}
