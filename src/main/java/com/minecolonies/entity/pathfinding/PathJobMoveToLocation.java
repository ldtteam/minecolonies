package com.minecolonies.entity.pathfinding;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.Log;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PathJobMoveToLocation extends PathJob
{
    protected final BlockPos destination;

    protected static final float DESTINATION_SLACK_NONE     = 0;
    protected static final float DESTINATION_SLACK_ADJACENT = 3.1F;    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    protected              float destinationSlack           = DESTINATION_SLACK_NONE; //  0 = exact match


    public PathJobMoveToLocation(World world, BlockPos start, BlockPos end, int range)
    {
        super(world, start, end, range);

        this.destination = new BlockPos(end);
    }

    /**
     * Perform the search
     *
     * @return PathEntity of a path to the given location, a best-effort, or null
     */
    @Override
    protected PathEntity search()
    {
        if (Configurations.pathfindingDebugVerbosity > DEBUG_VERBOSITY_NONE)
        {
            Log.logger.info(String.format("Pathfinding from [%d,%d,%d] to [%d,%d,%d]", start.getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()));
        }

        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination) != destination.getY())
        {
            destinationSlack = DESTINATION_SLACK_ADJACENT;
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(BlockPos pos)
    {
        int dx = pos.getX() - destination.getX();
        int dy = pos.getY() - destination.getY();
        int dz = pos.getZ() - destination.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 1.001D;
    }

    @Override
    protected boolean isAtDestination(Node n)
    {
        if (destinationSlack == DESTINATION_SLACK_NONE)
        {
            return n.pos.getX() == destination.getX() &&
                    n.pos.getY() == destination.getY() &&
                    n.pos.getZ() == destination.getZ();
        }

        return destination.distanceSq(n.pos.getX(), n.pos.getY(), n.pos.getZ()) <= destinationSlack;
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        //  For Result Score higher is better - return negative distance so closer to 0 = better
        return -destination.distanceSq(n.pos.getX(), n.pos.getY(), n.pos.getZ());
    }
}
