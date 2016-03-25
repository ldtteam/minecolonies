package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.ai.Tree;
import com.minecolonies.entity.ai.Water;
import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Find and return a path to the nearest tree
 * Created: May 21, 2015
 *
 * @author Raycoms
 */

//TODO Check if we already where at water (Save locations in fisherman class - call pathfinder with locationsList) - Max CAP of locations
public class PathJobFindWater extends PathJob
{
    public static class WaterPathResult extends PathResult
    {
        public ChunkCoordinates ponds;
    }

    ChunkCoordinates hutLocation;
    ArrayList<ChunkCoordinates> ponds = new ArrayList<>();

    /**
     * PathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param home   the position of the workers hut
     * @param range maximum path range
     * @param ponds already visited fishing places
     */
    public PathJobFindWater(World world, ChunkCoordinates start, ChunkCoordinates home, int range, ArrayList<ChunkCoordinates> ponds)
    {
        super(world, start, start, range, new WaterPathResult());
        this.ponds.addAll(ponds);
        hutLocation = home;
    }

    @Override
    public WaterPathResult getResult() { return (WaterPathResult)super.getResult(); }

    @Override
    protected double computeHeuristic(int x, int y, int z)
    {
        return 0;
    }

    @Override
    protected boolean isAtDestination(Node n)
    {
        ChunkCoordinates newPond = new ChunkCoordinates(n.x,n.y,n.z);

        if(n.parent == null || ponds.contains(newPond))
        {
            return false;
        }

        for(ChunkCoordinates pond: ponds)
        {
            if(!(squareDistance(pond,newPond) > 10))
            {
                return false;
            }
        }

        if(!ponds.stream().allMatch(pond -> squareDistance(pond,newPond) > 10))
        {
            return false;
        }

        if (n.x != n.parent.x)
        {
            int dx = n.x > n.parent.x ? 1 : -1;
            return isWater(n.x + dx, n.y, n.z) || isWater(n.x, n.y, n.z - 1) || isWater(n.x, n.y, n.z + 1);
        }
        else//z
        {
            int dz = n.z > n.parent.z ? 1 : -1;
            return isWater(n.x, n.y, n.z + dz) || isWater(n.x - 1, n.y, n.z) || isWater(n.x + 1, n.y, n.z);
        }
    }

    public float squareDistance(ChunkCoordinates currentPond, ChunkCoordinates nextPond)
    {
        return currentPond.getDistanceSquaredToChunkCoordinates(nextPond);
    }

    private boolean isWater(int x, int y, int z)
    {
        if(Water.checkWater(world, x, y, z))
        {
            getResult().ponds = new ChunkCoordinates(x, y, z);
            return true;
        }

        return false;
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        return 0;
    }
}

