package com.minecolonies.api.compatibility.tinkers;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
/*import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.world.block.SlimeDirtBlock;
import slimeknights.tconstruct.world.block.SlimeLeavesBlock;*/

/**
 * This class is to store a check to see if a tree is a slime tree.
 */
public final class SlimeTreeCheck extends SlimeTreeProxy
{
    /**
     * Check if block is slime block.
     *
     * @param block the block.
     * @return if the block is a slime block.
     */
    public static boolean isSlimeBlock(@NotNull final Block block)
    {
        return new SlimeTreeCheck().checkForTinkersSlimeBlock(block);
    }

    /**
     * Check if block is slime block.
     *
     * @param block the block.
     * @return if the block is a slime block.
     */
    /*@Override
    public boolean checkForTinkersSlimeBlock(@NotNull final Block block)
    {
        return block.defaultBlockState().is(TinkerTags.Blocks.SLIMY_LOGS);
    }*/

    /**
     * Check if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    /*@Override
    public boolean checkForTinkersSlimeLeaves(@NotNull final Block block)
    {
        return block.defaultBlockState().is(TinkerTags.Blocks.SLIMY_LEAVES);
    }*/

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    /*@Override
    public boolean checkForTinkersSlimeSapling(@NotNull final Block block)
    {
        return block.defaultBlockState().is(TinkerTags.Blocks.SLIMY_SAPLINGS);
    }*/

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    /*@Override
    public boolean checkForTinkersSlimeDirtOrGrass(@NotNull final Block block)
    {
        return block instanceof SlimeDirtBlock || block.defaultBlockState().is(TinkerTags.Blocks.SLIMY_GRASS);
    }*/

    /**
     * Get the Slime leaf variant.
     *
     * @param leaf the leaf.
     * @return the variant.
     */
    /*@Override
    public int getTinkersLeafVariant(@NotNull final BlockState leaf)
    {
        return ((SlimeLeavesBlock) leaf.getBlock()).getFoliageType().ordinal();
    }*/

    /**
     * Check if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    public static boolean isSlimeLeaf(@NotNull final Block block)
    {
        return new SlimeTreeCheck().checkForTinkersSlimeLeaves(block);
    }

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    public static boolean isSlimeSapling(@NotNull final Block block)
    {
        return new SlimeTreeCheck().checkForTinkersSlimeSapling(block);
    }

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    public static boolean isSlimeDirtOrGrass(@NotNull final Block block)
    {
        return new SlimeTreeCheck().checkForTinkersSlimeDirtOrGrass(block);
    }

    /**
     * Get the Slime leaf variant.
     *
     * @param leaf the leaf.
     * @return the variant.
     */
    public static int getLeafVariant(@NotNull final BlockState leaf)
    {
        return new SlimeTreeCheck().getTinkersLeafVariant(leaf);
    }
}
