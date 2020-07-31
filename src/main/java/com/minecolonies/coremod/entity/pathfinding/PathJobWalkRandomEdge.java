package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
    private static final Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    /**
     * The chance to check if the node is an edge, determines the random edge behaviour
     */
    private static final int NODE_EDGE_CHANCE = 10;

    public PathJobWalkRandomEdge(
      final World world,
      @NotNull final BlockPos start, final int range, final LivingEntity entity)
    {
        super(world, start, start, range, entity);
    }

    @Override
    protected double computeHeuristic(final BlockPos pos)
    {
        return entity.get().getPosition().manhattanDistance(pos);
    }

    @Override
    protected boolean isAtDestination(final Node n)
    {
        if (Math.abs(start.getY() - n.pos.getY()) > 6)
        {
            return false;
        }

        if (entity.get().getRNG().nextInt(NODE_EDGE_CHANCE) == 0)
        {
            for (final Direction direction : Direction.Plane.HORIZONTAL.)
            {
                if (world.isAirBlock(n.pos.down().offset(direction)))
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
        return entity.get().getPosition().manhattanDistance(n.pos);
    }

    @Override
    protected boolean isPassable(@NotNull final BlockState block)
    {
        if (block.getBlock() == Blocks.LADDER)
        {
            return false;
        }
        return super.isPassable(block);
    }
}
