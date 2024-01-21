package com.minecolonies.core.entity.pathfinding.pathjobs;

import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Moves to the given location, with a special passable check.
 */
public class PathJobMoveToWithPassable extends PathJobMoveToLocation
{
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
      @NotNull final BlockPos end, final int range, final LivingEntity entity, final Function<BlockState, Boolean> isPassable)
    {
        super(world, start, end, range, entity);
        this.isPassable = isPassable;
    }

    @Override
    protected boolean isPassable(@NotNull final BlockState block, final BlockPos pos, final MNode parent, final boolean head)
    {
        return super.isPassable(block, pos, parent, head) || isPassable.apply(block);
    }
}
