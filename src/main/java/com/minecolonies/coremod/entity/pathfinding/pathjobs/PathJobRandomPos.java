package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.pathfinding.Node;
import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.PathingConstants.DEBUG_VERBOSITY_NONE;

import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob.SurfaceType;

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
    private static Random random = new Random();

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
      final World world,
      @NotNull final BlockPos start,
      final int minDistFromStart,
      final int range,
      final LivingEntity entity)
    {
        super(world, start, start, range, new PathResult<PathJobRandomPos>(), entity);
        this.minDistFromStart = minDistFromStart;
        this.maxDistToDest = range;

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
      final World world,
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
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return Math.sqrt(destination.distSqr(new BlockPos(pos.getX(), destination.getY(), pos.getZ())));
    }

    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        if ((minDistFromStart ==  1 || start.distSqr(n.pos) > minDistFromStart * minDistFromStart) && isWalkableSurface(world.getBlockState(n.pos.below()), n.pos.below()) == SurfaceType.WALKABLE
              && destination.distSqr(n.pos) < this.maxDistToDest * this.maxDistToDest)
        {
            return true;
        }
        return false;
    }

    @Override
    protected double getNodeResultScore(@NotNull final Node n)
    {
        //  For Result Score lower is better
        return destination.distSqr(n.pos);
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
