package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.PathingConstants.DEBUG_VERBOSITY_NONE;

/**
 * Job that handles moving to a location.
 */
public class PathJobMoveToLocation extends AbstractPathJob {
    private static final float DESTINATION_SLACK_NONE = 0.1F;
    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    private static final float DESTINATION_SLACK_ADJACENT = (float) Math.sqrt(2f);
    private static final double TIE_BREAKER = 1.001D;
    @NotNull
    private final BlockPos destination;
    // 0 = exact match
    private float destinationSlack = DESTINATION_SLACK_NONE;

    /**
     * The manhattan distance between start and end.
     */
    private final double startEndDist;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world world the entity is in.
     * @param start starting location.
     * @param end   target location.
     * @param range max search range.
     * @param entity the entity.
     */
    public PathJobMoveToLocation(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntity entity)
    {
        super(world, start, end, range, entity);

        this.destination = new BlockPos(end);

        startEndDist = start.distanceSq(end);
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
        if (MineColonies.getConfig().getCommon().pathfindingDebugVerbosity.get() > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] to [%d,%d,%d]",
              start.getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()));
        }

        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination) != destination.getY())
        {
            destinationSlack = DESTINATION_SLACK_ADJACENT;
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return destination.manhattanDistance(pos);
    }

    /**
     * Checks if the target has been reached.
     *
     * @param n Node to test.
     * @return true if has been reached.
     */
    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        if (destinationSlack <= DESTINATION_SLACK_NONE)
        {
            return n.pos.getX() == destination.getX()
                     && n.pos.getY() == destination.getY()
                     && n.pos.getZ() == destination.getZ();
        }

        return destination.withinDistance(n.pos, DESTINATION_SLACK_ADJACENT);
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getNodeResultScore(@NotNull final Node n)
    {
        //  For Result Score higher is better
        return (startEndDist - destination.manhattanDistance(n.pos)) - 0.2 * n.getScore();
    }
}
