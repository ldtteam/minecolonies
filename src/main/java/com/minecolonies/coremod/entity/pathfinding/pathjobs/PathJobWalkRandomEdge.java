package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.coremod.entity.pathfinding.Node;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Walks to a random edge block nearby, a block next to air. Does not use ladders
 */
public class PathJobWalkRandomEdge extends AbstractPathJob
{
    /**
     * The chance to check if the node is an edge, determines the random edge behaviour
     */
    private static final int NODE_EDGE_CHANCE = 10;

    public PathJobWalkRandomEdge(
      final World world,
      @NotNull final BlockPos start, final int range, final LivingEntity entity)
    {
        super(world, AbstractPathJob.prepareStart(entity), start, range, entity);
    }

    @Override
    protected double computeHeuristic(final BlockPos pos)
    {
        return entity.get().blockPosition().distManhattan(pos);
    }

    @Override
    protected boolean isAtDestination(final Node n)
    {
        if (start.getY() - n.pos.getY() > 3)
        {
            return false;
        }

        if (entity.get().getRandom().nextInt(NODE_EDGE_CHANCE) == 0)
        {
            for (final Direction direction : Direction.Plane.HORIZONTAL)
            {
                if (world.isEmptyBlock(n.pos.below().relative(direction)))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected double getNodeResultScore(final Node n)
    {
        return start.distManhattan(n.pos);
    }
}
