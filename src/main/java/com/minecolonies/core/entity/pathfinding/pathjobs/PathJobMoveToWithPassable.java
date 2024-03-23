package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Moves to the given location, with a special passable check.
 */
public class PathJobMoveToWithPassable extends PathJobMoveToLocation
{
    /**
     * Additional cost to special passing rule
     */
    public static final double PASSING_COST = 3;

    /**
     * Function which tests if the given blockstate is passable
     */
    private Function<BlockState, Boolean> isPassable;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world      world the entity is in.
     * @param start      starting location.
     * @param end        target location.
     * @param range      max search range.
     * @param entity     the entity.
     * @param isPassable passable check
     */
    public PathJobMoveToWithPassable(
      final Level world,
      @NotNull final BlockPos start,
      @NotNull final BlockPos end, final int range, final Mob entity, final Function<BlockState, Boolean> isPassable)
    {
        super(world, start, end, range, entity);
        this.isPassable = isPassable;
    }

    @Override
    protected boolean isPassable(@NotNull final BlockState block, final int x, final int y, final int z, final MNode parent, final boolean head)
    {
        return super.isPassable(block, x, y, z, parent, head) || isPassable.apply(block);
    }

    @Override
    protected double modifyCost(
      final double cost,
      final MNode parent,
      final boolean swimstart,
      final boolean swimming,
      final int x,
      final int y,
      final int z,
      final BlockState state)
    {
        if (!state.isAir() && isPassable.apply(state))
        {
            return cost * PASSING_COST;
        }
        else
        {
            final BlockState above = cachedBlockLookup.getBlockState(x, y + 1, z);
            if (!above.isAir() && isPassable.apply(above))
            {
                return cost * PASSING_COST;
            }
        }

        return cost;
    }
}
