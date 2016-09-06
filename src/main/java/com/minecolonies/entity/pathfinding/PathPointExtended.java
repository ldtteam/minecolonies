package com.minecolonies.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

public class PathPointExtended extends PathPoint
{
    public boolean    isOnLadder   = false;
    //Should be instantiated to something he doesn't recognize as climbable.
    public EnumFacing ladderFacing = EnumFacing.DOWN;

    public PathPointExtended(@NotNull BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ());
    }
}
