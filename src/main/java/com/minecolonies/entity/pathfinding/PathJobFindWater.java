package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.ai.Pond;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Find and return a path to the nearest water
 * Created: March 25, 2016
 *
 * @author Raycoms
 */

public class PathJobFindWater extends PathJob
{
    private static final int MIN_DISTANCE = 40;
    private static final int MAX_RANGE = 250;

    public static class WaterPathResult extends PathResult
    {
        public BlockPos pond;
    }

    private BlockPos hutLocation;
    private ArrayList<BlockPos> ponds = new ArrayList<>();

    /**
     * PathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param home   the position of the workers hut
     * @param range maximum path range
     * @param ponds already visited fishing places
     */
    PathJobFindWater(World world, BlockPos start, BlockPos home, int range, List<BlockPos> ponds)
    {
        super(world, start, start, range, new WaterPathResult());
        this.ponds = new ArrayList<>(ponds);
        hutLocation = home;
    }

    @Override
    public WaterPathResult getResult() { return (WaterPathResult)super.getResult(); }

    @Override
    protected double computeHeuristic(int x, int y, int z)
    {
        int dx = x - hutLocation.getX();
        int dy = y - hutLocation.getY();
        int dz = z - hutLocation.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 0.501D ;
    }

    //Overrides the Superclass in order to find only ponds of water with follow the wished conditions
    @Override
    protected boolean isAtDestination(Node n)
    {
        if(n.parent == null)
        {
            return false;
        }

        if(squareDistance(hutLocation,new BlockPos(n.x,n.y,n.z))>MAX_RANGE)
        {
            return false;
        }

        if (n.x != n.parent.x)
        {
            int dx = n.x > n.parent.x ? 1 : -1;
            return isWater(n.x + dx, n.y-1, n.z) || isWater(n.x, n.y-1, n.z - 1) || isWater(n.x, n.y-1, n.z + 1);
        }
        else//z
        {
            int dz = n.z > n.parent.z ? 1 : -1;
            return isWater(n.x, n.y-1, n.z + dz) || isWater(n.x - 1, n.y-1, n.z) || isWater(n.x + 1, n.y-1, n.z);
        }
    }

    private boolean isWater(int x, int y, int z)
    {
        BlockPos newPond = new BlockPos(x,y,z);

        if(ponds.contains(newPond) || pondsAreNear(ponds,newPond))
        {
            return false;
        }

        Pond pond = Pond.createWater(world,newPond);

        if(pond != null)
        {
            getResult().pond = new BlockPos(x, y, z);
            return true;
        }

        return false;
    }

    private static double squareDistance(BlockPos currentPond, BlockPos nextPond)
    {
        return currentPond.distanceSq(nextPond.getX(),nextPond.getY(),nextPond.getZ());
    }

    private Predicate<BlockPos> generateDistanceFrom(int range, BlockPos newpond)
    {
        return pond -> squareDistance(pond, newpond) < range;
    }

    private boolean pondsAreNear(ArrayList<BlockPos> ponds, BlockPos newPond)
    {
        if(ponds.isEmpty())
        {
            return false;
        }
        Predicate<BlockPos> compare = generateDistanceFrom(MIN_DISTANCE, newPond);
        return ponds.stream().anyMatch(compare);
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        return 0;
    }
}

