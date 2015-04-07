package com.minecolonies.entity.pathfinding;

import net.minecraft.pathfinding.PathPoint;

public class PathPointExtended extends PathPoint
{
    public boolean isOnLadder   = false;
    public int     ladderFacing = 0;

    public PathPointExtended(int x, int y, int z)
    {
        super(x, y, z);
    }
}
