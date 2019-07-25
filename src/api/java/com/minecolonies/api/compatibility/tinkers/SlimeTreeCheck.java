package com.minecolonies.api.compatibility.tinkers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * This class is to store a check to see if a tree is a slime tree.
 */
public final class SlimeTreeCheck extends SlimeTreeProxy
{
    private static final String TCONSTRUCT = "tconstruct";

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
    @Override
    public boolean checkForTinkersSlimeBlock(@NotNull final Block block)
    {
        return false;
    }

    /**
     * Check if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    @Override
    public boolean checkForTinkersSlimeLeaves(@NotNull final Block block)
    {
        return false;
    }

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    @Override
    public boolean checkForTinkersSlimeSapling(@NotNull final Block block)
    {
        return false;
    }

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    @Override
    public boolean checkForTinkersSlimeDirtOrGrass(@NotNull final Block block)
    {
        return false;
    }

    /**
     * Get the Slime leaf variant.
     *
     * @param leaf the leaf.
     * @return the variant.
     */
    @Override
    public int getTinkersLeafVariant(@NotNull final BlockState leaf)
    {
        return 0;
    }

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