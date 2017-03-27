package com.minecolonies.compatibility.tinkers;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.world.TinkerWorld;

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
    @Override
    @Optional.Method(modid = "tconstruct")
    public boolean checkForTinkersSlimeBlock(@NotNull final Block block)
    {
        return block == TinkerCommons.blockSlimeCongealed;
    }

    /**
     * Check if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    @Override
    @Optional.Method(modid = "tconstruct")
    public boolean checkForTinkersSlimeLeaves(@NotNull final Block block)
    {
        return block == TinkerWorld.slimeLeaves;
    }

    /**
     * Check if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    @Override
    @Optional.Method(modid = "tconstruct")
    public boolean checkForTinkersSlimeSapling(@NotNull final Block block)
    {
        return block == TinkerWorld.slimeSapling;
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
}
