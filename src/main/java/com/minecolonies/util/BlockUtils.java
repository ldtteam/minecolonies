package com.minecolonies.util;

import net.minecraft.block.Block;
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
     * @param block block to be checked
     * @return true if is water.
     */
    public static boolean isWater(Block block)
    {
        return Objects.equals(block, Blocks.water)
               || Objects.equals(block, Blocks.flowing_water);
    }
}
