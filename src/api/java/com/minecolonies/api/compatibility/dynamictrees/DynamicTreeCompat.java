package com.minecolonies.api.compatibility.dynamictrees;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class DynamicTreeCompat extends DynamicTreeProxy
{

    private static DynamicTreeCompat instance = new DynamicTreeCompat();

    private static final String DYNAMIC_MODID = "dynamictrees";

    private static final String DYNAMIC_TREE_DAMAGE = "fallingtree";

    /**
     * Hashmap of fakeplayers, dimension-id as key
     */
    private static Map<Integer, FakePlayer> fakePlayers = new HashMap<>();

    private DynamicTreeCompat()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Check whether dynamic tree's mod is present
     * @return true
     */
    @Override
    protected boolean isDynamicTreePresent()
    {
        return true;
    }

    /**
     * Check whether dynamic tree's mod is present
     *
     * @return true or false
     */
    public static boolean isDynTreePresent()
    {
        return instance.isDynamicTreePresent();
    }

    /**
     * Check whether the block is part of a dynamic Tree
     *
     * @param block Block to check
     */
    @Override
    protected boolean checkForDynamicTreeBlock(@NotNull final Block block)
    {
        return false;
    }

    /**
     * Check wether the block is part of a dynamic Tree
     *
     * @param block Block to check
     */
    public static boolean isDynamicTreeBlock(final Block block)
    {
        return instance.checkForDynamicTreeBlock(block);
    }

    /**
     * Check wether the block is a dynamic leaf
     *
     * @param block Block to check
     */
    @Override
    protected boolean checkForDynamicLeavesBlock(final Block block)
    {
        return false;
    }

    /**
     * Check wether the block is a dynamic leaf
     *
     * @param block Block to check
     */
    public static boolean isDynamicLeavesBlock(final Block block)
    {
        return instance.checkForDynamicLeavesBlock(block);
    }

    /**
     * Check whether the block is a shell block.
     *
     * @param block the block to check
     * @return true if it is a shell block.
     */
    @Override
    protected boolean checkForDynamicTrunkShellBlock(final Block block)
    {
        return false;
    }

    /**
     * Check whether the block is a shell block.
     *
     * @param block the block to check
     * @return true if it is a shell block.
     */
    public static boolean isDynamicTrunkShellBlock(final Block block)
    {
        return instance.checkForDynamicTrunkShellBlock(block);
    }

    /**
     * Returns drops of a dynamic seed as List
     *
     * @param world      world the Leaf is in
     * @param pos        position of the Leaf
     * @param blockState Blockstate of the Leaf
     * @param fortune    amount of fortune to use
     * @param leaf       The leaf to check
     */
    @Override
    protected NonNullList<ItemStack> getDropsForLeaf(
      @NotNull final IWorld world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @NotNull final int fortune,
      @NotNull final Block leaf)
    {
        return NonNullList.create();
    }

    /**
     * Returns drops of a dynamic seed as List
     *
     * @param world      world the Leaf is in
     * @param pos        position of the Leaf
     * @param blockState Blockstate of the Leaf
     * @param fortune    amount of fortune to use
     * @param leaf       The leaf to check
     */
    public static NonNullList<ItemStack> getDropsForLeafCompat(final IWorld world, final BlockPos pos, final BlockState blockState, final int fortune, final Block leaf)
    {
        return instance.getDropsForLeaf(world, pos, blockState, fortune, leaf);
    }

    /**
     * Check wether the item is a dynamic Sapling
     *
     * @param item Item to check
     */
    @Override
    protected boolean checkForDynamicSapling(@NotNull final Item item)
    {
        return false;
    }

    /**
     * Check wether the item is a dynamic Sapling
     *
     * @param item Item to check
     */
    public static boolean isDynamicTreeSapling(final Item item)
    {
        return instance.checkForDynamicSapling(item);
    }

    /**
     * Check wether the Itemstack is a dynamic Sapling
     *
     * @param stack Itemstack to check
     * @return true if it is a dynamic Sapling
     */
    public static boolean isDynamicTreeSapling(final ItemStack stack)
    {
        return instance.checkForDynamicSapling(stack.getItem());
    }

    /**
     * Creates a runnable to harvest/break a dynamic tree
     *
     * @param world        The world the tree is in
     * @param blockToBreak The block of the dynamic tree
     * @param toolToUse    The tool to break the tree with, optional
     * @param workerPos    The position the fakeplayer breaks the tree from, optional
     * @return Runnable to break the Tree
     */
    @Override
    protected Runnable getTreeBreakActionCompat(@NotNull final World world, @NotNull final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos)
    {
        return () ->
        {

        };
    }

    /**
     * Creates a runnable to harvest/break a dynamic tree
     *
     * @param world        The world the tree is in
     * @param blockToBreak The block of the dynamic tree
     * @param toolToUse    The tool to break the tree with, optional
     * @param workerPos    The position the fakeplayer breaks the tree from, optional
     * @return Runnable to break the Tree
     */
    public static Runnable getTreeBreakAction(final World world, final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos)
    {
        return instance.getTreeBreakActionCompat(world, blockToBreak, toolToUse, workerPos);
    }

    /**
     * Tries to plant a sapling at the given location
     *
     * @param world        World to plant the sapling in
     * @param location     location to plant the sapling
     * @param saplingStack Itemstack of the sapling
     * @return true if successful
     */
    @Override
    protected boolean plantDynamicSaplingCompat(@NotNull final World world, @NotNull final BlockPos location, @NotNull final ItemStack saplingStack)
    {
        return false;
    }

    /**
     * Tries to plant a sapling at the given location
     *
     * @param world    World to plant the sapling in
     * @param location location to plant the sapling
     * @param sapling  Itemstack of the sapling
     * @return true if successful
     */
    public static boolean plantDynamicSapling(final World world, final BlockPos location, final ItemStack sapling)
    {
        return instance.plantDynamicSaplingCompat(world, location, sapling);
    }

    /**
     * Returns the damageType string falling dynamic Tree's use
     *
     * @return damageType
     */
    public static String getDynamicTreeDamage()
    {
        return DYNAMIC_TREE_DAMAGE;
    }

    /**
     * Method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @return true when same family
     */
    @Override
    protected boolean hasFittingTreeFamilyCompat(@NotNull final BlockPos block1, @NotNull final BlockPos block2, @NotNull final IWorld world)
    {
        return false;
    }

    /**
     * Method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @return true when same family
     */
    public static boolean hasFittingTreeFamily(@NotNull final BlockPos block1, @NotNull final BlockPos block2, @NotNull final IWorld world)
    {
        return instance.hasFittingTreeFamilyCompat(block1, block2, world);
    }
}
