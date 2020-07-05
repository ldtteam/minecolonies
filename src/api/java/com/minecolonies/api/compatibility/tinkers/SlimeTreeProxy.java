package com.minecolonies.api.compatibility.tinkers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * This is the fallback for when tinkers is not present!
 */
public class SlimeTreeProxy
{
    /**
     * This is the fallback for when tinkers is not present!
     *
     * @param block the block.
     * @return if the block is a slime block.
     */
    protected boolean checkForTinkersSlimeBlock(@NotNull final Block block)
    {
        return false;
    }

    /**
     * This is the fallback for when tinkers is not present!
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    protected boolean checkForTinkersSlimeLeaves(@NotNull final Block block)
    {
        return false;
    }

    /**
     * This is the fallback for when tinkers is not present!
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    protected boolean checkForTinkersSlimeSapling(@NotNull final Block block)
    {
        return false;
    }

    /**
     * This is the fallback for when tinkers is not present!
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    protected boolean checkForTinkersSlimeDirtOrGrass(@NotNull final Block block)
    {
        return false;
    }

    public int getTinkersLeafVariant(@NotNull final BlockState leaf)
    {
        return 0;
    }
}
