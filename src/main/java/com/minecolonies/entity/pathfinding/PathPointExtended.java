package com.minecolonies.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

public class PathPointExtended extends PathPoint
{
    public boolean    isOnLadder   = false;
    //Should be instantiated to something he doesn't recognize as climbable.
    public EnumFacing ladderFacing = EnumFacing.DOWN;

    public PathPointExtended(BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ());
    }
}
