package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class for all Block type checking
 */
public final class BlockUtils
{
    /**
     * Predicated to determine if a block is free to place
     */
    private static List<BiPredicate<Block, IBlockState>> freeToPlaceBlocks =
            Arrays.asList(
                    (block, iBlockState) -> block.equals(Blocks.air),
                    (block, iBlockState) -> block.getMaterial().isLiquid(),
                    (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
                    (block, iBlockState) -> block.equals(Blocks.leaves),
                    (block, iBlockState) -> block.equals(Blocks.leaves2),
                    (block, iBlockState) -> block.equals(Blocks.double_plant),
                    (block, iBlockState) -> block.equals(Blocks.grass),
                    (block, iBlockState) -> block instanceof BlockDoor
                                            && iBlockState != null
                                            && iBlockState.getValue(PropertyBool.create("upper"))

                         );


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
    public static boolean freeToPlace(final Block block)
    {
        return freeToPlace(block, null);
    }

    /**
     * Checks if this block type is something we can place for free
     * <p>
     * The builder uses this to determine if he need resources for the block
     *
     * @param block    the block to check
     * @param metadata the matadata this block has
     * @return true if we can just place it
     */
    public static boolean freeToPlace(final Block block, final IBlockState metadata)
    {
        if (block == null)
        {
            return true;
        }
        for (BiPredicate<Block, IBlockState> predicate : freeToPlaceBlocks)
        {
            if (predicate.test(block, metadata))
            {
                return true;
            }
        }
        return false;
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
