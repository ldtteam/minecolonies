package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.WaterPathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Pond;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Find and return a path to the nearest water. Created: March 25, 2016
 */
public class PathJobFindWater extends AbstractPathJob
{
    private static final int                                  MIN_DISTANCE = 40;
    private static final int                                  MAX_RANGE    = 100;
    private final        BlockPos                             hutLocation;
    @NotNull
    private final        ArrayList<Tuple<BlockPos, BlockPos>> ponds;

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param home   the position of the worker hut.
     * @param range  maximum path range.
     * @param ponds  already visited fishing places.
     * @param entity the entity.
     */
    public PathJobFindWater(
      final Level world,
      @NotNull final BlockPos start,
      final BlockPos home,
      final int range,
      @NotNull final List<Tuple<BlockPos, BlockPos>> ponds,
      final LivingEntity entity)
    {
        super(world, start, start, range, new WaterPathResult(), entity);
        this.ponds = new ArrayList<>(ponds);
        hutLocation = home;
    }

    private static double squareDistance(@NotNull final BlockPos currentPond, @NotNull final BlockPos nextPond)
    {
        return currentPond.distSqr(nextPond);
    }

    @NotNull
    @Override
    public WaterPathResult getResult()
    {
        return (WaterPathResult) super.getResult();
    }

    // TODO: recheck heuristic
    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        final int dx = x - hutLocation.getX();
        final int dy = y - hutLocation.getY();
        final int dz = z - hutLocation.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker - halved
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 0.501D;
    }

    //Overrides the Superclass in order to find only ponds of water with follow the wished conditions
    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        if (BlockPosUtil.distSqr(hutLocation, n.x, n.y, n.z) > MAX_RANGE * MAX_RANGE)
        {
            return false;
        }

        if (isWater(n))
        {
            getResult().parent = new BlockPos(n.x, n.y, n.z);
            getResult().isEmpty = ponds.isEmpty();
            return true;
        }

        return false;
    }

    /**
     * Checks if a certain location is water.
     *
     * @param n the location.
     * @return true if so.
     */
    private boolean isWater(@NotNull final MNode n)
    {
        if (n.parent == null)
        {
            return false;
        }

        if (cachedBlockLookup.getBlockState(n.x, n.y, n.z).getBlock() != Blocks.WATER && cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z).getBlock() != Blocks.WATER)
        {
            if (n.x == n.parent.x)
            {
                final int dz = n.z > n.parent.z ? 1 : -1;
                return Pond.checkWater(cachedBlockLookup, tempWorldPos.set(n.x, n.y - 1, n.z + dz), getResult()) || Pond.checkWater(cachedBlockLookup,
                  tempWorldPos.set(n.x - 1, n.y - 1, n.z),
                  getResult()) || Pond.checkWater(cachedBlockLookup,
                  tempWorldPos.set(n.x + 1, n.y - 1, n.z),
                  getResult());
            }
            else
            {
                final int dx = n.x > n.parent.x ? 1 : -1;
                return Pond.checkWater(cachedBlockLookup, tempWorldPos.set(n.x + dx, n.y - 1, n.z), getResult()) || Pond.checkWater(cachedBlockLookup,
                  tempWorldPos.set(n.x, n.y - 1, n.z - 1),
                  getResult()) || Pond.checkWater(cachedBlockLookup,
                  tempWorldPos.set(n.x, n.y - 1, n.z + 1),
                  getResult());
            }
        }

        // TODO: Recheck condition, might want a different data structure than a list<tuple> aswell? since creating a new tuple and searching the whole list is kinda slow
        return getResult().pond != null && !ponds.contains(new Tuple<>(new BlockPos(n.x, n.y, n.z), new BlockPos(n.parent.x, n.parent.y, n.parent.z))) && !pondsAreNear(ponds,
          tempWorldPos.set(n.x, n.y, n.z));
    }

    /**
     * Checks if there are close ponds to a position.
     *
     * @param ponds   all ponds.
     * @param newPond the position.
     * @return true if so.
     */
    private static boolean pondsAreNear(@NotNull final ArrayList<Tuple<BlockPos, BlockPos>> ponds, @NotNull final BlockPos newPond)
    {
        if (ponds.isEmpty())
        {
            return false;
        }

        for (Tuple<BlockPos, BlockPos> p : ponds)
        {
            if (squareDistance(p.getB(), newPond) < MIN_DISTANCE * MIN_DISTANCE)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected double getNodeResultScore(final MNode n)
    {
        return 0;
    }
}

