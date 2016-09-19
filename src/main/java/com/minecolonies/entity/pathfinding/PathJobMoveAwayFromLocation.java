package com.minecolonies.entity.pathfinding;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.Log;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Job that handles moving away from something.
 */
public class PathJobMoveAwayFromLocation extends AbstractPathJob
{
    private static final double TIE_BREAKER = 1.001D;

    @NotNull
    protected final BlockPos avoid;
    @NotNull
    protected final BlockPos heuristicPoint;
    protected final int      avoidDistance;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world         world the entity is in.
     * @param start         starting location.
     * @param avoid         location to avoid.
     * @param avoidDistance how far to move away.
     * @param range         max range to search.
     */
    public PathJobMoveAwayFromLocation(World world, @NotNull BlockPos start, @NotNull BlockPos avoid, int avoidDistance, int range)
    {
        super(world, start, avoid, range);

        this.avoid = new BlockPos(avoid);
        this.avoidDistance = avoidDistance;

        double dx = (double) (start.getX() - avoid.getX());
        double dz = (double) (start.getZ() - avoid.getZ());

        double scalar = avoidDistance / Math.sqrt(dx * dx + dz * dz);
        dx *= scalar;
        dz *= scalar;

        heuristicPoint = new BlockPos(start.getX() + (int) dx, start.getY(), start.getZ() + (int) dz);
    }

    /**
     * Perform the search
     *
     * @return Path of a path to the given location, a best-effort, or null
     */
    @Nullable
    @Override
    protected Path search()
    {
        if (Configurations.pathfindingDebugVerbosity > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] away from [%d,%d,%d]",
                    start.getX(), start.getY(), start.getZ(), avoid.getX(), avoid.getY(), avoid.getZ()));
        }

        return super.search();
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight
     *
     * @param pos Position to compute heuristic from
     * @return heuristic as a double - Manhatten Distance with tie-breaker
     */
    @Override
    protected double computeHeuristic(@NotNull BlockPos pos)
    {
        int dx = pos.getX() - heuristicPoint.getX();
        int dy = pos.getY() - heuristicPoint.getY();
        int dz = pos.getZ() - heuristicPoint.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * TIE_BREAKER;
    }

    @Override
    protected boolean isAtDestination(@NotNull Node n)
    {
        return getNodeResultScore(n) >= (avoidDistance * avoidDistance);
    }

    @Override
    protected double getNodeResultScore(@NotNull Node n)
    {
        return avoid.distanceSq(n.pos.getX(), n.pos.getY(), n.pos.getZ());
    }
}
