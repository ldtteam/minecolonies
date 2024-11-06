package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.entity.pathfinding.MNode;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Walks to a random edge block nearby, a block next to air. Does not use ladders
 */
public class PathJobWalkRandomEdge extends AbstractPathJob implements ISearchPathJob
{
    /**
     * The chance to check if the node is an edge, determines the random edge behaviour
     */
    private static final int NODE_EDGE_CHANCE = 10;

    /**
     * The position we want to search around from, usually guarding pos, so guide the heuristic there from the current entity position
     */
    private final BlockPos searchAroundPos;

    public PathJobWalkRandomEdge(
      final Level world,
      @NotNull final BlockPos searchAroundPos, final int range, final Mob entity)
    {
        super(world, PathfindingUtils.prepareStart(entity), range, new PathResult<PathJobWalkRandomEdge>(), entity);
        this.searchAroundPos = searchAroundPos;
    }

    @Override
    protected double computeHeuristic(final int x, final int y, final int z)
    {
        return BlockPosUtil.distManhattan(searchAroundPos, x, y, z);
    }

    @Override
    protected boolean isAtDestination(final MNode n)
    {
        if (searchAroundPos.getY() - n.y > 3)
        {
            return false;
        }

        if (ColonyConstants.rand.nextInt(NODE_EDGE_CHANCE) == 0)
        {
            for (final Direction direction : Direction.Plane.HORIZONTAL)
            {
                if (cachedBlockLookup.getBlockState(n.x + direction.getStepX(), n.y - 1 + direction.getStepY(), n.z + direction.getStepZ()).isAir())
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected double getEndNodeScore(final MNode n)
    {
        return BlockPosUtil.distManhattan(start, n.x, n.y, n.z);
    }
}
