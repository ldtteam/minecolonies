package com.minecolonies.api.compatibility.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.minecolonies.api.util.Log;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class DynamicTreeCompat extends DynamicTreeProxy
{

    private static DynamicTreeCompat instance = new DynamicTreeCompat();

    private static final String DYNAMIC_MODID = "dynamictrees";

    private static final String DYNAMIC_TREE_DAMAGE = "fallingtree";

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
    @Optional.Method(modid = DYNAMIC_MODID)
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
    @Optional.Method(modid = DYNAMIC_MODID)
    protected boolean checkForDynamicTreeBlock(@NotNull final Block block)
    {
        return (block instanceof BlockBranch);
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
    @Optional.Method(modid = DYNAMIC_MODID)
    protected boolean checkForDynamicLeavesBlock(final Block block)
    {
        return block instanceof BlockDynamicLeaves;
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
     * Returns drops of a dynamic seed as List
     *
     * @param world      world the Leaf is in
     * @param pos        position of the Leaf
     * @param blockState Blockstate of the Leaf
     * @param fortune    amount of fortune to use
     * @param leaf       The leaf to check
     */
    @Override
    @Optional.Method(modid = DYNAMIC_MODID)
    protected NonNullList<ItemStack> getDropsForLeaf(
      @NotNull final IBlockAccess world,
      @NotNull final BlockPos pos,
      @NotNull final IBlockState blockState,
      @NotNull final int fortune,
      @NotNull final Block leaf)
    {
        if (isDynamicLeavesBlock(leaf))
        {
            final NonNullList<ItemStack> list = NonNullList.create();
            list.addAll(((BlockDynamicLeaves) leaf).getDrops(world, pos, blockState, fortune));
            return list;
        }
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
    public static NonNullList<ItemStack> getDropsForLeafCompat(final IBlockAccess world, final BlockPos pos, final IBlockState blockState, final int fortune, final Block leaf)
    {
        return instance.getDropsForLeaf(world, pos, blockState, fortune, leaf);
    }

    /**
     * Check wether the item is a dynamic Sapling
     *
     * @param item Item to check
     */
    @Override
    @Optional.Method(modid = DYNAMIC_MODID)
    protected boolean checkForDynamicSapling(@NotNull final Item item)
    {
        return (item instanceof Seed);
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
    @Optional.Method(modid = DYNAMIC_MODID)
    protected Runnable getTreeBreakActionCompat(@NotNull final World world, @NotNull final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos)
    {
        return () ->
        {
            final IBlockState curBlockState = world.getBlockState(blockToBreak);
            @Nullable final Block curBlock = curBlockState.getBlock();

            if (world.getMinecraftServer() == null)
            {
                Log.getLogger().error("Minecolonies:DynamicTreeCompat unexpected null while trying to get World");
                return;
            }
            final FakePlayer fake =
              new FakePlayer(world.getMinecraftServer().getWorld(world.provider.getDimension()), new GameProfile(UUID.randomUUID(), "minecolonies_LumberjackFake"));

            if (workerPos != null)
            {
                fake.setPosition(workerPos.getX(), workerPos.getY(), workerPos.getZ());
            }

            if (toolToUse != null)
            {
                fake.setHeldItem(EnumHand.MAIN_HAND, toolToUse);
            }

            curBlock.removedByPlayer(curBlockState, world, blockToBreak, fake, true);
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
    @Optional.Method(modid = DYNAMIC_MODID)
    protected boolean plantDynamicSaplingCompat(@NotNull World world, @NotNull BlockPos location, @NotNull ItemStack saplingStack)
    {
        if (saplingStack.getItem() instanceof Seed)
        {
            return ((Seed) saplingStack.getItem()).getSpecies(saplingStack).plantSapling(world, location);
        }
        else
        {
            return false;
        }
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
}
