package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import net.minecraft.block.Block;
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
     * Checks if this block type should be destroyed.
     * <p>
     * The builder uses this to check if he should clear this block.
     *
     * @param block the block type to check
     * @return true if you should back away
     */
    public static boolean shouldNeverBeMessedWith(Block block)
    {
        return Objects.equals(block, Blocks.air)
               || block instanceof AbstractBlockHut
               || Objects.equals(block, Blocks.bedrock);
    }

    /**
     * Checks if this block type is something we can place for free
     * <p>
     * The builder uses this to determine if he need resources for the block
     *
     * @param block the block to check
     * @return true if we can just place it
     */
    public static boolean freeToPlace(Block block)
    {
        return block.getMaterial().isLiquid()
               || BlockUtils.isWater(block.getDefaultState())
               || block.equals(Blocks.leaves)
               || block.equals(Blocks.leaves2)
               || block.equals(Blocks.double_plant)
               || block.equals(Blocks.grass);
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
