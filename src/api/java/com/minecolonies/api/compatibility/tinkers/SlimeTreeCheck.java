package com.minecolonies.api.compatibility.tinkers;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.world.block.*;

import java.util.List;
import java.util.function.Function;

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
    public boolean checkForTinkersSlimeBlock(@NotNull final Block block)
    {
        return block instanceof StrippableLogBlock;
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
        return block instanceof SlimeLeavesBlock;
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
        return block instanceof SlimeSaplingBlock;
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
        return block instanceof SlimeDirtBlock || block instanceof SlimeGrassBlock;
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
        return ((SlimeLeavesBlock) leaf.getBlock()).getFoliageType().ordinal();
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

    public static NonNullList<ItemStack> getDropsForLeaf(
      @NotNull final IWorld world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @NotNull final Block leaf)
    {
        if (isSlimeLeaf(leaf))
        {
            final NonNullList<ItemStack> list = NonNullList.create();
            // Implementation is chance based, so repeat till we get an item
            for (int i = 0; i < 100 && list.isEmpty(); i++)
            {
                List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld) world, pos, null);
                for (ItemStack itemStack : drops)
                {
                    if (SlimeSaplingBlock.getBlockFromItem(itemStack.getItem()) instanceof SlimeSaplingBlock)
                    {
                        list.add(itemStack);
                    }
                }
            }
            return list;
        }
        return NonNullList.create();
    }

    /**
     *
     */
    public static Block getSlimeDirtBlock()
    {
        return new SlimeDirtBlock(AbstractBlock.Properties.create(null, (Function<BlockState, MaterialColor>) null));
    }
}
