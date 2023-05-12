package com.minecolonies.coremod.entity.pathfinding.pathjobs;

import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Walks to a random edge block nearby, a block next to air. Does not use ladders
 */
public class PathJobWalkRandomEdge extends AbstractPathJob
{
    /**
     * The chance to check if the node is an edge, determines the random edge behaviour
     */
    private static final int NODE_EDGE_CHANCE = 10;

    /**
     * Walk-job specific random.
     */
    private static final Random random = new Random();

    public PathJobWalkRandomEdge(
      final Level world,
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
    protected boolean isAtDestination(final MNode n)
    {
        if (start.getY() - n.pos.getY() > 3)
        {
            return false;
        }

        if (random.nextInt(NODE_EDGE_CHANCE) == 0)
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
    protected double getNodeResultScore(final MNode n)
    {
        return start.distManhattan(n.pos);
    }
}
