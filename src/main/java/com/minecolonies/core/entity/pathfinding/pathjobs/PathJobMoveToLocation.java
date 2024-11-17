package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ShapeUtil;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.navigation.IDynamicHeuristicNavigator;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Job that handles moving to a location.
 */
public class PathJobMoveToLocation extends AbstractPathJob implements IDestinationPathJob
{
    private static final float    DESTINATION_SLACK_NONE     = 0.1F;
    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    private static final float    DESTINATION_SLACK_ADJACENT = (float) Math.sqrt(2f);
    @NotNull
    protected final BlockPos destination;
    // 0 = exact match
    private              float    destinationSlack           = DESTINATION_SLACK_NONE;

    /**
     * Modifier to the heuristics
     */
    private double heuristicModifier = 1;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world  world the entity is in.
     * @param start  starting location.
     * @param end    target location.
     * @param range  max search range.
     * @param entity the entity.
     */
    public PathJobMoveToLocation(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final Mob entity)
    {
        super(world, start, end, new PathResult<PathJobMoveToLocation>(), entity);

        maxNodes += range;
        this.destination = new BlockPos(end);

        if (entity != null && entity.getNavigation() instanceof IDynamicHeuristicNavigator)
        {
            heuristicModifier = ((IDynamicHeuristicNavigator) entity.getNavigation()).getAvgHeuristicModifier();
        }

        // Overestimate for long distances, +1 per 100 blocks
        heuristicModifier += BlockPosUtil.distManhattan(start, end) / 100.0;
        extraNodes = 4;
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
        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination.getX(), destination.getY(), destination.getZ()) != destination.getY())
        {
            destinationSlack = DESTINATION_SLACK_ADJACENT;
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(destination, x, y, z) * heuristicModifier;
    }

    /**
     * Checks if the target has been reached.
     *
     * @param n Node to test.
     * @return true if has been reached.
     */
    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        boolean atDest = false;
        if (destinationSlack <= DESTINATION_SLACK_NONE)
        {
            atDest = n.x == destination.getX()
                       && n.y == destination.getY()
                       && n.z == destination.getZ();
        }
        else if (n.y == destination.getY() - 1)
        {
            atDest = BlockPosUtil.distSqr(destination, n.x, destination.getY(), n.z) < DESTINATION_SLACK_ADJACENT * DESTINATION_SLACK_ADJACENT;
        }
        else
        {
            atDest = BlockPosUtil.distSqr(destination, n.x, n.y, n.z) < DESTINATION_SLACK_ADJACENT * DESTINATION_SLACK_ADJACENT;
        }

        if (atDest)
        {
            atDest = SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                       == SurfaceType.WALKABLE;
        }

        return atDest;
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getEndNodeScore(@NotNull final MNode n)
    {
        if (PathfindingUtils.isLiquid(cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z)))
        {
            return BlockPosUtil.distManhattan(destination, n.x, n.y, n.z) + 30;
        }

        if (!ShapeUtil.isEmpty(cachedBlockLookup.getBlockState(n.x, n.y, n.z).getCollisionShape(cachedBlockLookup, tempWorldPos.set(n.x, n.y, n.z))))
        {
            return BlockPosUtil.distManhattan(destination, n.x, n.y, n.z) + 10;
        }

        //  For Result Score lower is better

        int xDist = Math.abs(destination.getX() - n.x);
        int yDist = Math.abs(destination.getY() - n.y);
        int zDist = Math.abs(destination.getZ() - n.z);
        return xDist + yDist + zDist;
    }

    @Override
    protected boolean stopOnNodeLimit(final int totalNodesVisited, final MNode bestNode, final int nodesSinceEndNode)
    {
        // Small chance to go full limit to maybe find a path still, when we did not find any good nodes to move towards
        if (totalNodesVisited < MAX_NODES && BlockPosUtil.distManhattan(start, bestNode.x, bestNode.y, bestNode.z) < 10 && ColonyConstants.rand.nextInt(100) <= 20)
        {
            maxNodes += 1000;
            return false;
        }
        // 10k limit for progressing
        else if (nodesSinceEndNode < 200 && totalNodesVisited < MAX_NODES * 2)
        {
            maxNodes += 500;
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return super.toString() + " destination:" + destination;
    }

    @Override
    public BlockPos getDestination()
    {
        return destination;
    }
}
