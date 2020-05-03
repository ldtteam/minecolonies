package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
      final World world,
      @NotNull final BlockPos start,
      @NotNull final BlockPos end, final int range, final LivingEntity entity, final Function<BlockState, Boolean> isPassable)
    {
        super(world, start, end, range, entity);
        this.isPassable = isPassable;
    }

    @Override
    protected boolean isPassable(@NotNull final BlockState block)
    {
        return super.isPassable(block) || isPassable.apply(block);
    }
}
