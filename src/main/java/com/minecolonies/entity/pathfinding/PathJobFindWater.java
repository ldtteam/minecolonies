package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.ai.Water;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Find and return a path to the nearest water
 * Created: March 25, 2016
 *
 * @author Raycoms
 */

public class PathJobFindWater extends PathJob
{
    private static final int MIN_DISTANCE = 10;

    public static class WaterPathResult extends PathResult
    {
        public ChunkCoordinates ponds;
    }

    private ChunkCoordinates hutLocation;
    private ArrayList<ChunkCoordinates> ponds = new ArrayList<>();

    /**
     * PathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param home   the position of the workers hut
     * @param range maximum path range
     * @param ponds already visited fishing places
     */
    PathJobFindWater(World world, ChunkCoordinates start, ChunkCoordinates home, int range, ArrayList<ChunkCoordinates> ponds)
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

    //Overrides the Superclass in order to find only ponds of water with follow the wished conditions
    @Override
    protected boolean isAtDestination(Node n)
    {
        ChunkCoordinates newPond = new ChunkCoordinates(n.x,n.y,n.z);
        ChunkCoordinates landBlock = findLandBlockBesidesWater(n);

        if(n.parent == null || ponds.contains(newPond))
        {
            return false;
        }

        if(pondsAreNear(ponds,newPond))
        {
            return false;
        }

        if(landBlock == null)
        {
            return false;
        }

        if(!world.isAirBlock(n.x,n.y+1,n.z))
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

    private ChunkCoordinates findLandBlockBesidesWater(Node n)
    {
        for(int x = -1; x <= 1; x++)
        {
            for(int z = -1; z <= 1; z++) {
                if (world.getBlock(n.x + x, n.y, n.z + z).getMaterial().isSolid())
                {
                    return new ChunkCoordinates(n.x + x, n.y, n.z + z);
                }
            }
        }
        return null;
    }
    private float squareDistance(ChunkCoordinates currentPond, ChunkCoordinates nextPond)
    {
        return currentPond.getDistanceSquaredToChunkCoordinates(nextPond);
    }

    private Predicate<ChunkCoordinates> generateDistanceFrom(int range, ChunkCoordinates newpond)
    {
        return pond -> squareDistance(pond, newpond) > range;
    }

    private boolean pondsAreNear(ArrayList<ChunkCoordinates> ponds, ChunkCoordinates newPond)
    {
        Predicate<ChunkCoordinates> compare = generateDistanceFrom(MIN_DISTANCE, newPond);
        return !ponds.stream().anyMatch(compare);
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

