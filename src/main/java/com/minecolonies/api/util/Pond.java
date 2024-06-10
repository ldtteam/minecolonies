package com.minecolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to search for fisher ponds.
 */
public final class Pond
{
    /**
     * The minimum pond requirements.
     */
    public static final int WATER_POOL_WIDTH_REQUIREMENT  = 5;
    public static final int WATER_POOL_LENGTH_REQUIREMENT = 5;
    public static final int WATER_DEPTH_REQUIREMENT       = 4;

    /**
     * Checks if on position "water" really is water, if the water is connected to land and if the pond is big enough (bigger then 20).
     *
     * @param world The world the player is in.
     * @param water The coordinate to check.
     * @return true if water.
     */
    public static boolean checkWater(@NotNull final BlockGetter world, @NotNull final BlockPos water)
    {
        final BlockPos.MutableBlockPos tempPos = water.mutable();

        for (int i = 1; i < WATER_DEPTH_REQUIREMENT; i++)
        {
            if (world.getBlockState(tempPos.set(water.getX(), water.getY() - i, water.getZ())).getBlock() != Blocks.WATER)
            {
                return false;
            }
        }

        for (int x = -WATER_POOL_WIDTH_REQUIREMENT / 2; x < WATER_POOL_WIDTH_REQUIREMENT / 2; x++)
        {
            for (int z = -WATER_POOL_LENGTH_REQUIREMENT; z < WATER_POOL_LENGTH_REQUIREMENT / 2; z++)
            {
                tempPos.set(water.getX() + x, water.getY(), water.getZ() + z);
                final BlockState state = world.getBlockState(tempPos);

                if (!isWaterForFishing(world, state, tempPos))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the water is fine for fishing, see vanilla FishingHook checks
     *
     * @param world
     * @param state
     * @param pos
     * @return
     */
    public static boolean isWaterForFishing(final BlockGetter world, final BlockState state, final BlockPos pos)
    {
        if (!state.isAir() && !state.is(Blocks.LILY_PAD))
        {
            FluidState fluidstate = state.getFluidState();
            return fluidstate.is(FluidTags.WATER) && fluidstate.isSource() && state.getCollisionShape(world, pos).isEmpty();
        }

        return false;
    }
}
