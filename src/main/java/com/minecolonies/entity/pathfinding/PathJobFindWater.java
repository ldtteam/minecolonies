package com.minecolonies.entity.pathfinding;

import com.minecolonies.entity.ai.citizen.fisherman.Pond;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Find and return a path to the nearest water
 * Created: March 25, 2016
 *
 * @author Raycoms
 */

public class PathJobFindWater extends AbstractPathJob
{
    private static final int MIN_DISTANCE = 40;
    private static final int MAX_RANGE    = 250;
    private BlockPos hutLocation;
    @NotNull
    private ArrayList<BlockPos> ponds = new ArrayList<>();

    /**
     * AbstractPathJob constructor
     *
     * @param world the world within which to path
     * @param start the start position from which to path from
     * @param home  the position of the workers hut
     * @param range maximum path range
     * @param ponds already visited fishing places
     */
    PathJobFindWater(World world, @NotNull BlockPos start, BlockPos home, int range, @NotNull List<BlockPos> ponds)
    {
        super(world, start, start, range, new WaterPathResult());
        this.ponds = new ArrayList<>(ponds);
        hutLocation = home;
    }

    public static class WaterPathResult extends PathResult
    {
        public BlockPos pond;
        public boolean  isEmpty;
    }

    private static double squareDistance(@NotNull BlockPos currentPond, @NotNull BlockPos nextPond)
    {
        return currentPond.distanceSq(nextPond.getX(), nextPond.getY(), nextPond.getZ());
    }

    @NotNull
    @Override
    public WaterPathResult getResult() { return (WaterPathResult) super.getResult(); }

    @Override
    protected double computeHeuristic(@NotNull BlockPos pos)
    {
        int dx = pos.getX() - hutLocation.getX();
        int dy = pos.getY() - hutLocation.getY();
        int dz = pos.getZ() - hutLocation.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 0.501D;
    }

    //Overrides the Superclass in order to find only ponds of water with follow the wished conditions
    @Override
    protected boolean isAtDestination(@NotNull Node n)
    {
        if (n.parent == null)
        {
            return false;
        }

        if (squareDistance(hutLocation, n.pos) > MAX_RANGE)
        {
            return false;
        }

        if (n.pos.getX() != n.parent.pos.getX())
        {
            int dx = n.pos.getX() > n.parent.pos.getX() ? 1 : -1;
            return isWater(n.pos.add(dx, -1, 0)) || isWater(n.pos.add(0, -1, -1)) || isWater(n.pos.add(0, -1, 1));
        }
        else//z
        {
            int dz = n.pos.getZ() > n.parent.pos.getZ() ? 1 : -1;
            return isWater(n.pos.add(0, -1, dz)) || isWater(n.pos.add(-1, -1, 0)) || isWater(n.pos.add(1, -1, 0));
        }
    }

    private boolean isWater(@NotNull BlockPos newPond)
    {
        if (ponds.contains(newPond) || pondsAreNear(ponds, newPond))
        {
            return false;
        }

        @Nullable Pond pond = Pond.createWater(world, newPond);

        if (pond != null)
        {
            getResult().pond = newPond;
            getResult().isEmpty = ponds.isEmpty();
            return true;
        }

        return false;
    }

    private static Predicate<BlockPos> generateDistanceFrom(int range, @NotNull BlockPos newpond)
    {
        return pond -> squareDistance(pond, newpond) < range;
    }

    private boolean pondsAreNear(@NotNull ArrayList<BlockPos> ponds, @NotNull BlockPos newPond)
    {
        if (ponds.isEmpty())
        {
            return false;
        }
        @NotNull Predicate<BlockPos> compare = generateDistanceFrom(MIN_DISTANCE, newPond);
        return ponds.stream().anyMatch(compare);
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        return 0;
    }
}

