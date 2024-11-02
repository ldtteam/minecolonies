package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Pond;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.entity.pathfinding.pathresults.WaterPathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Find and return a path to the nearest water. Created: March 25, 2016
 */
public class PathJobFindWater extends AbstractPathJob
{
    private static final int                             MAX_RANGE    = 100;
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
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        super.setPathingOptions(pathingOptions);
        getPathingOptions().swimCostEnter = 0;
        getPathingOptions().swimCost = 0;
    }

    /**
     * Simple reverse lookup to find a fitting shore for a pond location
     */
    private class PathJobFindFishingPos extends AbstractPathJob
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
            Vec3 vec3d = new Vec3(start.getX(), start.getY() + 1.8, start.getZ());
            Vec3 vec3d1 = new Vec3(n.x, n.y, n.z);
            return this.world.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
        }
    }
}

