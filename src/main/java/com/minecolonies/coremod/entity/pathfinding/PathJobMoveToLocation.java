package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.LivingEntityBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Job that handles moving to a location.
 */
public class PathJobMoveToLocation extends AbstractPathJob
{
    private static final float  DESTINATION_SLACK_NONE     = 0.1F;
    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    private static final float  DESTINATION_SLACK_ADJACENT = 3.1F;
    private static final double TIE_BREAKER                = 1.001D;
    @NotNull
    private final BlockPos destination;
    // 0 = exact match
    private float destinationSlack = DESTINATION_SLACK_NONE;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world world the entity is in.
     * @param start starting location.
     * @param end   target location.
     * @param range max search range.
     * @param entity the entity.
     */
    public PathJobMoveToLocation(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntityBase entity)
    {
        super(world, start, end, range, entity);

        this.destination = new BlockPos(end);
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
        if (Configurations.pathfinding.pathfindingDebugVerbosity > DEBUG_VERBOSITY_NONE)
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
        final int dx = pos.getX() - destination.getX();
        final int dy = pos.getY() - destination.getY();
        final int dz = pos.getZ() - destination.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * TIE_BREAKER;
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

        return destination.distanceSq(n.pos.getX(), n.pos.getY(), n.pos.getZ()) <= destinationSlack;
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
        //  For Result Score higher is better - return negative distance so closer to 0 = better
        return -destination.distanceSq(n.pos.getX(), n.pos.getY(), n.pos.getZ());
    }
}
