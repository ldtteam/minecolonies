package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.PathingConstants.DEBUG_VERBOSITY_NONE;

/**
 * Job that handles random pathing.
 */
public class PathJobRandomPos extends AbstractPathJob
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
     * Random pathing rand.
     */
    private static final RandomSource random = RandomSource.createThreadSafe();

    /**
     * Minimum distance to the goal.
     */
    private final int maxDistToDest;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world    world the entity is in.
     * @param start    starting location.
     * @param minDistFromStart how far to move away.
     * @param range    max range to search.
     * @param entity   the entity.
     */
    public PathJobRandomPos(
      final Level world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int range,
      final LivingEntity entity)
    {
        super(world, start, start, range, new PathResult<PathJobRandomPos>(), entity);
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = -1;

        final Tuple<Direction, Direction> dir = BlockPosUtil.getRandomDirectionTuple(random);
        this.destination = start.relative(dir.getA(), minDistFromStart).relative(dir.getB(), minDistFromStart);
    }

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world            world the entity is in.
     * @param start            starting location.
     * @param minDistFromStart how far to move away.
     * @param searchRange            max range to search.
     * @param entity           the entity.
     */
    public PathJobRandomPos(
      final Level world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int searchRange,
      final int maxDistToDest,
      final LivingEntity entity,
      @NotNull final BlockPos dest)
    {
        super(world, start, dest, searchRange, new PathResult<PathJobRandomPos>(), entity);
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = maxDistToDest;
        this.destination = dest;
    }

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world    world the entity is in.
     * @param start    starting location.
     * @param minDistFromStart how far to move away.
     * @param range    max range to search.
     * @param entity   the entity.
     */
    public PathJobRandomPos(
      final Level world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int range,
      final LivingEntity entity,
      final BlockPos startRestriction,
      final BlockPos endRestriction,
      final AbstractAdvancedPathNavigate.RestrictionType restrictionType)
    {
        super(world, start, startRestriction, endRestriction, range, false, new PathResult<PathJobRandomPos>(), entity, restrictionType);

        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = -1;

        final Tuple<Direction, Direction> dir = BlockPosUtil.getRandomDirectionTuple(random);
        this.destination = start.relative(dir.getA(), minDistFromStart).relative(dir.getB(), minDistFromStart);
    }

    @Nullable
    @Override
    protected Path search()
    {
        if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] in the direction of [%d,%d,%d]",
              start.getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()));
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.dist(destination, x, y, z);
    }

    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        if (random.nextInt(minDistFromStart * minDistFromStart) == 0
              && isInRestrictedArea(n.x, n.y, n.z)
              && BlockPosUtil.distSqr(start, n.x, n.y, n.z) > minDistFromStart * minDistFromStart
              && SurfaceType.getSurfaceType(cachedBlockLookup, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z)) == SurfaceType.WALKABLE
              && (maxDistToDest == -1 || BlockPosUtil.distSqr(destination, n.x, n.y, n.z) < this.maxDistToDest * this.maxDistToDest)
              && !SurfaceType.isWater(cachedBlockLookup, tempWorldPos.set(n.x, n.y - 1, n.z)))
        {
            return true;
        }
        return false;
    }

    @Override
    protected double getNodeResultScore(@NotNull final MNode n)
    {
        return 0;
    }
    
    /**
     * Checks if position and range match the given parameters
     *
     * @param range max dist to dest range
     * @param pos   dest to look from
     * @return
     */
    public boolean posAndRangeMatch(final int range, final BlockPos pos)
    {
        return destination != null && pos != null && range == maxDistToDest && destination.equals(pos);
    }
}
