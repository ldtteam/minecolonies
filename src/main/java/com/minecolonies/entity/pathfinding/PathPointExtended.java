package com.minecolonies.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class PathPointExtended extends PathPoint
{
    public boolean    isOnLadder   = false;
    public EnumFacing ladderFacing = EnumFacing.NORTH;

    public PathPointExtended(BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ());
    }
}
