package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.PathingConstants.DEBUG_VERBOSITY_NONE;

/**
 * Job that handles moving away from something.
 */
public class PathJobMoveAwayFromLocation extends AbstractPathJob
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
      final LivingEntity entity)
    {
        super(world, start, avoid, range, entity);

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
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    @Override
    protected Path search()
    {
        if (MineColonies.getConfig().getServer().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] away from [%d,%d,%d]",
              start.getX(), start.getY(), start.getZ(), avoid.getX(), avoid.getY(), avoid.getZ()));
        }

        return super.search();
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight.
     *
     * @return heuristic as a double - Manhatten Distance with tie-breaker.
     */
    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return Math.sqrt(BlockPosUtil.distSqr(preferredDirection, x, y, z) + 100 / Math.max(1, BlockPosUtil.dist(avoid, x, y, z)));
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
        return BlockPosUtil.dist(avoid, n.x, n.y, n.z) > avoidDistance;
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double amount.
     */
    @Override
    protected double getNodeResultScore(@NotNull final MNode n)
    {
        return -BlockPosUtil.distSqr(avoid, n.x, n.y, n.z);
    }
}
