package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/**
 * Job that handles random pathing.
 */
public class PathJobRandomPos extends AbstractPathJob implements IDestinationPathJob
{
    /**
     * Direction to walk to.
     */
    @NotNull
    protected final BlockPos destination;

    /**
     * Required avoidDistance.
     */
    protected final int minDistFromStart;

    /**
     * Minimum distance to the goal.
     */
    private final int maxDistToDest;

    /**
     * If inside paths should be preferred.
     */
    private boolean preferInside = false;

    /**
     * Box restriction area
     */
    private AABB     restrictionBox = null;
    private BlockPos restrictionBoxCenter = null;

    /**
     * Mutable blockpos to use where necessary.
     */
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world            world the entity is in.
     * @param start            starting location.
     * @param minDistFromStart how far to move away.
     * @param range            max range to search.
     * @param entity           the entity.
     */
    public PathJobRandomPos(
      final Level world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int range,
      final Mob entity)
    {
        super(world, start, range, new PathResult<PathJobRandomPos>(), entity);
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = -1;

        this.destination = BlockPosUtil.getRandomPosAround(start, minDistFromStart);
    }

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world            world the entity is in.
     * @param start            starting location.
     * @param minDistFromStart how far to move away.
     * @param searchRange      max range to search.
     * @param entity           the entity.
     */
    public PathJobRandomPos(
      final Level world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int searchRange,
      final int maxDistToDest,
      final Mob entity,
      @NotNull final BlockPos dest)
    {
        super(world, start, searchRange, new PathResult<PathJobRandomPos>(), entity);
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = maxDistToDest;
        this.destination = dest;
    }

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world            world the entity is in.
     * @param start            starting location.
     * @param minDistFromStart how far to move away.
     * @param range            max range to search.
     * @param entity           the entity.
     */
    public PathJobRandomPos(
      final Level world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int range,
      final Mob entity,
      final BlockPos startRestriction,
      final BlockPos endRestriction)
    {
        super(world, start, range, new PathResult<PathJobRandomPos>(), entity);

        restrictionBox = new AABB(Math.min(startRestriction.getX(), endRestriction.getX()),
          Math.min(startRestriction.getY(), endRestriction.getY()),
          Math.min(startRestriction.getZ(), endRestriction.getZ()),
          Math.max(startRestriction.getX(), endRestriction.getX()),
          Math.max(startRestriction.getY(), endRestriction.getY()),
          Math.max(startRestriction.getZ(), endRestriction.getZ()));
        restrictionBoxCenter = BlockPos.containing(restrictionBox.getCenter());
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = -1;

        this.destination = BlockPosUtil.getRandomPosAround(start, minDistFromStart);
    }

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world            world the entity is in.
     * @param start            starting location.
     * @param minDistFromStart how far to move away.
     * @param range            max range to search.
     * @param entity           the entity.
     * @param preferInside        if the entity should try to stay inside.
     */
    public PathJobRandomPos(
        final Level world,
        @NotNull final BlockPos start,
        final int minDistFromStart,
        final int range,
        final Mob entity,
        final BlockPos startRestriction,
        final BlockPos endRestriction,
        final boolean preferInside)
    {
        super(world, start, range, new PathResult<PathJobRandomPos>(), entity);

        restrictionBox = new AABB(Math.min(startRestriction.getX(), endRestriction.getX()),
            Math.min(startRestriction.getY(), endRestriction.getY()),
            Math.min(startRestriction.getZ(), endRestriction.getZ()),
            Math.max(startRestriction.getX(), endRestriction.getX()),
            Math.max(startRestriction.getY(), endRestriction.getY()),
            Math.max(startRestriction.getZ(), endRestriction.getZ()));
        restrictionBoxCenter = BlockPos.containing(restrictionBox.getCenter());
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = -1;
        this.preferInside = preferInside;
        this.destination = BlockPosUtil.getRandomPosAround(start, minDistFromStart);
    }

    /**
     * Check if there is space a few blocks above without solid blocks.
     * @param x x pos.
     * @param y y pos.
     * @param z z pos.
     * @return true if so.
     */
    private boolean hasSpaceAbove(final int x, final int y, final int z)
    {
        for (int i = 1; i < 5; i++)
        {
            if (cachedBlockLookup.getBlockState(x,y+i+1,z).isSolid())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        if (restrictionBox != null)
        {
            return (BlockPosUtil.distManhattan(destination, x, y, z) + BlockPosUtil.distManhattan(restrictionBoxCenter, x, y, z) / 2.0);
        }

        return BlockPosUtil.distManhattan(destination, x, y, z);
    }

    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        if ((restrictionBox == null || restrictionBox.contains(n.x, n.y, n.z))
              && BlockPosUtil.distSqr(start, n.x, n.y, n.z) > minDistFromStart * minDistFromStart
              && (maxDistToDest == -1 || BlockPosUtil.distSqr(destination, n.x, n.y, n.z) < this.maxDistToDest * this.maxDistToDest)
              && (getPathingOptions().canWalkUnderWater() || !PathfindingUtils.isWater(cachedBlockLookup, tempWorldPos.set(n.x, n.y - 1, n.z)))
              && SurfaceType.getSurfaceType(cachedBlockLookup, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                   == SurfaceType.WALKABLE)
        {
            if (preferInside && hasSpaceAbove(n.x,n.y,n.z))
            {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    protected double getEndNodeScore(@NotNull final MNode n)
    {
        return BlockPosUtil.distManhattan(start, n.x, n.y, n.z);
    }

    @Override
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        super.setPathingOptions(pathingOptions);
        getPathingOptions().canDrop = false;
    }

    @Override
    public BlockPos getDestination()
    {
        return destination;
    }

    /**
     * Helper to compare if the given random pos job matches the input parameters
     *
     * @return true if the given job is the same
     */
    public static boolean isJobFor(final AbstractPathJob job, final BlockPos center, final int range)
    {
        if (job instanceof PathJobRandomPos pathJob)
        {
            return pathJob.destination != null && pathJob.destination.equals(center) && pathJob.maxDistToDest == range;
        }

        return false;
    }
}
