package com.minecolonies.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Objects;

/**
 * Utility class for all Block type checking
 */
public final class BlockUtils
{
    /**
     * Private constructor to hide the public one
     */
    private BlockUtils()
    {
    }

    /**
     * Checks if the block is water
     *
     * @param iBlockState block state to be checked
     * @return true if is water.
     */
    public static boolean isWater(IBlockState iBlockState)
    {
        return Objects.equals(iBlockState, Blocks.water.getDefaultState())
               || Objects.equals(iBlockState, Blocks.flowing_water.getDefaultState());
    }
}
