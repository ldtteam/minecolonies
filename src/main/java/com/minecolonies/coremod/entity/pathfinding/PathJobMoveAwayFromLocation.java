package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Job that handles moving away from something.
 */
public class PathJobMoveAwayFromLocation extends AbstractPathJob
{
    private static final double TIE_BREAKER    = 1.001D;

    /**
     * All directions to try to avoid to.
     */
    private static final int DIRECTIONS_TO_TRY = 4;

    /**
     * Position to run to, in order to avoid something.
     */
    @NotNull
    protected final BlockPos avoid;

    /**
     * Heuristic point used for calculation.
     */
    @NotNull
    protected final BlockPos heuristicPoint;

    /**
     * Required avoidDistance.
     */
    protected final int avoidDistance;

    /**
     * Direction he should run off to.
     */
    private final EnumFacing direction;

    /**
     * Random object.
     */
    private static final Random rand = new Random();

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world         world the entity is in.
     * @param start         starting location.
     * @param avoid         location to avoid.
     * @param avoidDistance how far to move away.
     * @param range         max range to search.
     */
    public PathJobMoveAwayFromLocation(final World world, @NotNull final BlockPos start, @NotNull final BlockPos avoid, final int avoidDistance, final int range)
    {
        super(world, start, avoid, range);

        this.avoid = new BlockPos(avoid);
        this.avoidDistance = avoidDistance;

        double dx = (double) (start.getX() - avoid.getX());
        double dz = (double) (start.getZ() - avoid.getZ());

        final double scalar = avoidDistance / Math.sqrt(dx * dx + dz * dz);
        dx *= scalar;
        dz *= scalar;

        final int randomValue = rand.nextInt(DIRECTIONS_TO_TRY);

        if(randomValue == 0)
        {
            heuristicPoint = new BlockPos(start.getX() + (int) dx, start.getY(), start.getZ());
            direction = EnumFacing.EAST;
        }
        else if(randomValue == 1)
        {
            heuristicPoint = new BlockPos(start.getX() - (int) dx, start.getY(), start.getZ());
            direction = EnumFacing.WEST;
        }
        else if(randomValue == 2)
        {
            heuristicPoint = new BlockPos(start.getX(), start.getY(), start.getZ() - (int) dz);
            direction = EnumFacing.NORTH;
        }
        else
        {
            heuristicPoint = new BlockPos(start.getX(), start.getY(), start.getZ() + (int) dz);
            direction = EnumFacing.SOUTH;
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
        if (Configurations.pathfindingDebugVerbosity > DEBUG_VERBOSITY_NONE)
        {
            Log.getLogger().info(String.format("Pathfinding from [%d,%d,%d] away from [%d,%d,%d]",
              start.getX(), start.getY(), start.getZ(), avoid.getX(), avoid.getY(), avoid.getZ()));
        }

        return super.search();
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight.
     *
     * @param pos Position to compute heuristic from.
     * @return heuristic as a double - Manhatten Distance with tie-breaker.
     */
    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        final int dx = pos.getX() - heuristicPoint.getX();
        final int dy = pos.getY() - heuristicPoint.getY();
        final int dz = pos.getZ() - heuristicPoint.getZ();

        //  Manhattan Distance with a 1/1000th tie-breaker
        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) * TIE_BREAKER;
    }

    /**
     * Checks if the destination has been reached.
     * Meaning that the avoid distance has been reached.
     *
     * @param n Node to test.
     * @return true if so.
     */
    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        final BlockPos vector = n.pos.subtract(avoid);
        return getNodeResultScore(n) >= (avoidDistance * avoidDistance) && EnumFacing.getFacingFromVector(vector.getX(), 0, vector.getZ()).equals(direction);
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double amount.
     */
    @Override
    protected double getNodeResultScore(@NotNull final Node n)
    {
        return avoid.distanceSq(n.pos.getX(), n.pos.getY(), n.pos.getZ());
    }
}
