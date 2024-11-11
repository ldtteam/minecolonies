package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Pond;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.entity.pathfinding.pathresults.WaterPathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Find and return a path to the nearest water. Created: March 25, 2016
 */
public class PathJobFindWater extends AbstractPathJob implements ISearchPathJob
{
    private static final int MAX_RANGE = 100;
    private final        BlockPos                        hutLocation;
    @NotNull
    private final        List<Tuple<BlockPos, BlockPos>> ponds;

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
        final Mob entity)
    {
        super(world, start, range, new WaterPathResult(), entity);
        this.ponds = new ArrayList<>(ponds);
        hutLocation = home;
    }

    @NotNull
    @Override
    public WaterPathResult getResult()
    {
        return (WaterPathResult) super.getResult();
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(hutLocation, x, y, z);
    }

    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        if (BlockPosUtil.distSqr(hutLocation, n.x, n.y, n.z) > MAX_RANGE * MAX_RANGE)
        {
            return false;
        }

        final MutableBlockPos problemPos = debugDrawEnabled ? BlockPos.ZERO.mutable() : null;
        if (n.isSwimming() && Pond.checkPond(world, tempWorldPos.set(n.x, n.y - 1, n.z), problemPos))
        {
            for (Tuple<BlockPos, BlockPos> existingPond : ponds)
            {
                if (BlockPosUtil.distManhattan(existingPond.getA(), n.x, n.y, n.z) < Pond.WATER_POOL_WIDTH_REQUIREMENT + 2)
                {
                    return false;
                }
            }

            final PathJobFindFishingPos job = new PathJobFindFishingPos(getActualWorld(), world, new BlockPos(n.x, n.y, n.z), hutLocation, 10);
            job.setPathingOptions(getPathingOptions());
            final Path path = job.search();
            if (path != null && path.canReach())
            {
                getResult().pond = new BlockPos(n.x, n.y, n.z);
                getResult().parent = path.getTarget();
                return true;
            }
        }

        // node is not pond -> debug
        if (problemPos != null && !problemPos.equals(BlockPos.ZERO))
        {
            debugNodesExtra.add(new MNode(n, problemPos.getX(), problemPos.getY(), problemPos.getZ(), -1, -1));
        }

        return false;
    }

    @Override
    protected double modifyCost(
        final double cost,
        final MNode parent,
        final boolean swimstart,
        final boolean swimming,
        final int x,
        final int y,
        final int z,
        final BlockState state, final BlockState below)
    {
        if (BlockPosUtil.distSqr(hutLocation, x, y, z) > MAX_RANGE * MAX_RANGE)
        {
            return cost * 10;
        }

        return cost;
    }

    @Override
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        super.setPathingOptions(pathingOptions);
        getPathingOptions().swimCostEnter = 0;
        getPathingOptions().swimCost = 0;
    }

    @Override
    public double getEndNodeScore(final MNode n)
    {
        return BlockPosUtil.distManhattan(hutLocation, n.x, n.y, n.z);
    }

    /**
     * Simple reverse lookup to find a fitting shore for a pond location
     */
    private class PathJobFindFishingPos extends AbstractPathJob implements ISearchPathJob
    {
        private final BlockPos direction;
        private final int      distance;

        public PathJobFindFishingPos(
            final Level actualWorld,
            final LevelReader world,
            final @NotNull BlockPos start,
            final @NotNull BlockPos direction,
            final int distance)
        {
            super(actualWorld, world, start, distance + 100, new PathResult(), null);
            this.direction = direction;
            this.distance = distance;
        }

        @Override
        protected void handleDebugOptions(final MNode node)
        {
            PathJobFindWater.this.handleDebugOptions(node);
        }

        @Override
        protected double computeHeuristic(final int x, final int y, final int z)
        {
            return BlockPosUtil.distManhattan(direction, x, y, z);
        }

        @Override
        protected boolean isAtDestination(final MNode n)
        {
            return !n.isSwimming()
                && BlockPosUtil.distManhattan(start, n.x, n.y, n.z) < distance
                && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                == SurfaceType.WALKABLE && BlockUtils.isAnySolid(cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z))
                && canSeeTargetFromPos(n);
        }

        /**
         * Checks visibility
         *
         * @param n
         * @return
         */
        private boolean canSeeTargetFromPos(final MNode n)
        {
            return !PathfindingUtils.hasAnyCollisionAlong(start.getX(), start.getY(), start.getZ(), n.x, n.y + 1, n.z, cachedBlockLookup);
        }

        @Override
        public double getEndNodeScore(final MNode n)
        {
            return BlockPosUtil.distManhattan(start, n.x, n.y, n.z);
        }
    }
}

