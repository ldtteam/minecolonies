package com.minecolonies.entity.pathfinding;

import com.minecolonies.MineColonies;
import com.minecolonies.configuration.Configurations;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class PathJobMoveToLocation extends PathJob
{
    protected final ChunkCoordinates destination;

    protected static final float DESTINATION_SLACK_NONE     = 0;
    protected static final float DESTINATION_SLACK_ADJACENT = 3.1F;    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    protected              float destinationSlack           = DESTINATION_SLACK_NONE; //  0 = exact match


    public PathJobMoveToLocation(World world, ChunkCoordinates start, ChunkCoordinates end, int range)
    {
        super(world, start, end, range);

        this.destination = new ChunkCoordinates(end);
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
            MineColonies.logger.info(String.format("Pathfinding from [%d,%d,%d] to [%d,%d,%d]", start.posX, start.posY, start.posZ, destination.posX, destination.posY, destination.posZ));
        }

        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination.posX, destination.posY, destination.posZ) != destination.posY)
        {
            destinationSlack = DESTINATION_SLACK_ADJACENT;
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(int x, int y, int z)
    {
        int dx = x - destination.posX;
        int dy = y - destination.posY;
        int dz = z - destination.posZ;

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 1.001D;
    }

    @Override
    protected boolean isAtDestination(Node n)
    {
        if (destinationSlack == DESTINATION_SLACK_NONE)
        {
            return n.x == destination.posX &&
                    n.y == destination.posY &&
                    n.z == destination.posZ;
        }

        return destination.getDistanceSquared(n.x, n.y, n.z) <= destinationSlack;
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        //  For Result Score higher is better - return negative distance so closer to 0 = better
        return -destination.getDistanceSquared(n.x, n.y, n.z);
    }
}
