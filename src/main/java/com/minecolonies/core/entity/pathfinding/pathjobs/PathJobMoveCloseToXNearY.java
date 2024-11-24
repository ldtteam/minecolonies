package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.SurfaceType;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

/**
 * Job that handles moving close to a position near another
 */
public class PathJobMoveCloseToXNearY extends AbstractPathJob implements IDestinationPathJob
{
    /**
     * Position to go close to
     */
    public final BlockPos desiredPosition;

    /**
     * Position to stay nearby
     */
    public final BlockPos nearbyPosition;

    /**
     * Required distance to reach
     */
    public final int distToDesired;

    public PathJobMoveCloseToXNearY(
      final Level world,
      final BlockPos desiredPosition,
      final BlockPos nearbyPosition,
      final int distToDesired,
      final Mob entity)
    {
        super(world, PathfindingUtils.prepareStart(entity), desiredPosition, new PathResult<PathJobMoveCloseToXNearY>(), entity);

        this.desiredPosition = desiredPosition;
        this.nearbyPosition = nearbyPosition;
        this.distToDesired = distToDesired;
        extraNodes = 20;
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(desiredPosition, x, y, z) + BlockPosUtil.distManhattan(nearbyPosition, x, y, z) * 2;
    }

    @Override
    protected boolean isAtDestination(@NotNull final MNode n)
    {
        if (desiredPosition.getX() == n.x && desiredPosition.getZ() == n.z)
        {
            return false;
        }

        return BlockPosUtil.distManhattan(desiredPosition, n.x, n.y, n.z) < distToDesired
                 && SurfaceType.getSurfaceType(world, cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z), tempWorldPos.set(n.x, n.y - 1, n.z), getPathingOptions())
                      == SurfaceType.WALKABLE;
    }

    @Override
    protected double getEndNodeScore(@NotNull final MNode n)
    {
        if (desiredPosition.getX() == n.x && desiredPosition.getZ() == n.z)
        {
            return 1000;
        }

        double dist = BlockPosUtil.distManhattan(desiredPosition, n.x, n.y, n.z) * 2 + BlockPosUtil.distManhattan(nearbyPosition, n.x, n.y, n.z);
        if (n.isSwimming())
        {
            dist += 50;
        }
        else if (cachedBlockLookup.getBlockState(n.x, n.y - 1, n.z) == Blocks.WATER.defaultBlockState())
        {
            dist += 50;
        }

        return dist;
    }

    @Override
    protected boolean stopOnNodeLimit(final int totalNodesVisited, final MNode bestNode, final int nodesSinceEndNode)
    {
        if (nodesSinceEndNode > 200)
        {
            return true;
        }
        else
        {
            maxNodes += 200;
            return false;
        }
    }

    @Override
    public BlockPos getDestination()
    {
        return desiredPosition;
    }
}
