package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Job that handles moving away from something.
 */
public class PathJobMoveAwayFromLocation extends AbstractPathJob implements IDestinationPathJob
{
    /**
     * Position to run to, in order to avoid something.
     */
    @NotNull
    protected final BlockPos avoid;
    /**
     * Required avoidDistance.
     */
    protected final int      avoidDistance;

    /**
     * The blockposition we're trying to move away to
     */
    private BlockPos preferredDirection;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world         world the entity is in.
     * @param start         starting location.
     * @param avoid         location to avoid.
     * @param avoidDistance how far to move away.
     * @param range         max range to search.
     * @param entity        the entity.
     */
    public PathJobMoveAwayFromLocation(
      final Level world,
      @NotNull final BlockPos start,
      @NotNull final BlockPos avoid,
      final int avoidDistance,
      final int range,
      final Mob entity)
    {
        super(world, start, range, new PathResult<PathJobMoveAwayFromLocation>(), entity);

        this.avoid = new BlockPos(avoid);
        this.avoidDistance = avoidDistance;

        preferredDirection = entity.blockPosition().offset(entity.blockPosition().subtract(avoid).multiply(range));
        if (entity instanceof AbstractEntityCitizen)
        {
            final IColony colony = ((AbstractEntityCitizen) entity).getCitizenColonyHandler().getColony();
            if (colony != null)
            {
                preferredDirection = colony.getCenter();
            }
        }
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight.
     *
     * @return heuristic as a double - Manhatten Distance with tie-breaker.
     */
    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.dist(preferredDirection, x, y, z);
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
        if (BlockPosUtil.dist(avoid, x, y, z) < 3)
        {
            return cost + 100;
        }

        return cost;
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
        return BlockPosUtil.dist(avoid, n.x, n.y, n.z) > avoidDistance
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                      == SurfaceType.WALKABLE;
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double amount.
     */
    @Override
    protected double getEndNodeScore(@NotNull final MNode n)
    {
        return -BlockPosUtil.dist(avoid, n.x, n.y, n.z);
    }

    @Override
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        super.setPathingOptions(pathingOptions);
        pathingOptions.dropCost = 5;
    }

    @Override
    public BlockPos getDestination()
    {
        return preferredDirection;
    }
}
