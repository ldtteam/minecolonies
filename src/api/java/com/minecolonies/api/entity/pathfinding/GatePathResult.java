package com.minecolonies.api.entity.pathfinding;

import net.minecraft.core.BlockPos;

/**
 * Contains the result of the path job to find a gate.
 */
public class GatePathResult extends PathResult
{
    /**
     * The position of the parent (stand block).
     */
    public BlockPos parent;

    /**
     * The position of the gate.
     */
    public BlockPos gate;

    /**
     * If the pond is empty.
     */
    public boolean isEmpty;
}
