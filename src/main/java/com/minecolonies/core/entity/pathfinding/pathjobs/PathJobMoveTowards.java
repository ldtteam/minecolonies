package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Job that handles moving in a direction
 */
public class PathJobMoveTowards extends AbstractPathJob implements IDestinationPathJob
{
    /**
     * Position to run to, in order to
     */
    protected final BlockPos target;
    /**
     * Required avoidDistance.
     */
    protected final int      minDistance;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world       world the entity is in.
     * @param start       starting location.
     * @param direction   location to avoid.
     * @param minDistance how far to move away.
     * @param range       max range to search.
     * @param entity      the entity.
     */
    public PathJobMoveTowards(
      final Level world,
      @NotNull final BlockPos start,
      @NotNull final BlockPos direction,
      final int minDistance,
      final Mob entity)
    {
        super(world, start, minDistance * 2, new PathResult<PathJobMoveTowards>(), entity);

        this.target = direction;
        this.minDistance = minDistance;
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight.
     *
     * @return heuristic as a double - Manhatten Distance with tie-breaker.
     */
    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(target, x, y, z);
    }

    /**
     * Checks if the destination has been reached. Meaning that the avoid distance has been reached.
     *
     * @param n Node to test.
     * @return true if so.
     */
    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        return BlockPosUtil.distManhattan(start, n.x, n.y, n.z) > minDistance
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                      == SurfaceType.WALKABLE;
    }

    @Override
    public BlockPos getDestination()
    {
        return target;
    }
}
