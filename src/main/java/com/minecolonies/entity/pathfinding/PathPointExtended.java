package com.minecolonies.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;

public class PathPointExtended extends PathPoint
{
    public boolean    isOnLadder   = false;
    public EnumFacing ladderFacing = EnumFacing.NORTH;

    public PathPointExtended(int x, int y, int z)
    {
        super(x, y, z);
    }
}
