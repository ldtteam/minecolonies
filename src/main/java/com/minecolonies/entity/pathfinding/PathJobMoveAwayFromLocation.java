package com.minecolonies.entity.pathfinding;

import com.minecolonies.MineColonies;
import com.minecolonies.configuration.Configurations;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class PathJobMoveAwayFromLocation extends PathJob
{
    protected final ChunkCoordinates avoid;
    protected final ChunkCoordinates heuristicPoint;
    protected final int avoidDistance;

    public PathJobMoveAwayFromLocation(World world, ChunkCoordinates start, ChunkCoordinates avoid, int avoidDistance, int range)
    {
        super(world, start, avoid, range);

        this.avoid = new ChunkCoordinates(avoid);
        this.avoidDistance = avoidDistance;

        double dx = start.posX - avoid.posX;
        double dz = start.posZ - avoid.posZ;

        double scalar = avoidDistance / Math.sqrt(dx * dx + dz * dz);
        dx *= scalar;
        dz *= scalar;

        heuristicPoint = new ChunkCoordinates(start.posX + (int)dx, start.posY, start.posZ + (int)dz);
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
            MineColonies.logger.info(String.format("Pathfinding from [%d,%d,%d] away from [%d,%d,%d]", start.posX, start.posY, start.posZ, avoid.posX, avoid.posY, avoid.posZ));
        }

        return super.search();
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight
     * @param x,y,z Position to compute heuristic from
     * @return
     */
    @Override
    protected double computeHeuristic(int x, int y, int z)
    {
        int dx = x - heuristicPoint.posX;
        int dy = y - heuristicPoint.posY;
        int dz = z - heuristicPoint.posZ;

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * 1.001D;
//        return Math.sqrt(avoid.getDistanceSquaredToChunkCoordinates(start) / avoid.getDistanceSquared(x, y, z));
    }

    @Override
    protected boolean isAtDestination(Node n)
    {
        return getNodeResultScore(n) >= (avoidDistance * avoidDistance);
    }

    @Override
    protected double getNodeResultScore(Node n)
    {
        return avoid.getDistanceSquared(n.x, n.y, n.z);
    }
}
