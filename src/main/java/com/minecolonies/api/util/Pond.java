package com.minecolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to search for fisher ponds.
 */
public final class Pond
{
    //TODO: Recheck logic/rewrite it simpler, return central pond position instead of the water pos next to the standing for better direction

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

                if (state.getBlock() != Blocks.WATER)
                {
                    return false;
                }
            }
        }

        return true;
    }
}
