package com.minecolonies.api.compatibility.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.minecolonies.api.util.Log;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    @Optional.Method(modid = DYNAMIC_MODID)
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
    @Optional.Method(modid = DYNAMIC_MODID)
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
    @Optional.Method(modid = DYNAMIC_MODID)
    protected NonNullList<ItemStack> getDropsForLeaf(
      @NotNull final IBlockAccess world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @NotNull final int fortune,
      @NotNull final Block leaf)
    {
        if (isDynamicLeavesBlock(leaf))
        {
            final NonNullList<ItemStack> list = NonNullList.create();
            // Implementation is chance based, so repeat till we get an item
            for (int i = 0; i < 100 && list.isEmpty(); i++)
            {
                list.addAll(((BlockDynamicLeaves) leaf).getDrops(world, pos, blockState, fortune));
            }
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
    public static NonNullList<ItemStack> getDropsForLeafCompat(final IBlockAccess world, final BlockPos pos, final BlockState blockState, final int fortune, final Block leaf)
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
            final BlockState curBlockState = world.getBlockState(blockToBreak);
            @Nullable final Block curBlock = curBlockState.getBlock();

            if (world.getMinecraftServer() == null)
            {
                Log.getLogger().error("Minecolonies:DynamicTreeCompat unexpected null while trying to get World");
                return;
            }

            final int dim = world.provider.getDimension();
            FakePlayer fake = fakePlayers.get(dim);

            if (fake == null)
            {
                fakePlayers.put(dim, new FakePlayer(world.getMinecraftServer().getWorld(dim),
                  new GameProfile(UUID.randomUUID(), "minecolonies_LumberjackFake")));
                fake = fakePlayers.get(dim);
            }

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
    protected boolean plantDynamicSaplingCompat(@NotNull final World world, @NotNull final BlockPos location, @NotNull final ItemStack saplingStack)
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

    /**
     * Method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @return true when same family
     */
    @Override
    @Optional.Method(modid = DYNAMIC_MODID)
    protected boolean hasFittingTreeFamilyCompat(@NotNull final BlockPos block1, @NotNull final BlockPos block2, @NotNull final IBlockAccess world)
    {
        TreeFamily fam1 = getFamilyForBlock(block1, world);
        TreeFamily fam2 = getFamilyForBlock(block2, world);

        if (fam1 != null && fam2 != null)
        {
            return fam1 == fam2;
        }
        return false;
    }

    /**
     * Returns the dynamic tree family for the give blockpos
     *
     * @param blockPos position
     * @param world    blockaccess
     * @return dynamic tree family
     */
    private static TreeFamily getFamilyForBlock(@NotNull final BlockPos blockPos, @NotNull final IBlockAccess world)
    {
        final Block block = world.getBlockState(blockPos).getBlock();
        if (block instanceof BlockBranch)
        {
            return ((BlockBranch) block).getFamily();
        }
        if (block instanceof BlockDynamicLeaves)
        {
            return ((BlockDynamicLeaves) block).getFamily(world.getBlockState(blockPos), world, blockPos);
        }

        return null;
    }

    /**
     * Method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @return true when same family
     */
    public static boolean hasFittingTreeFamily(@NotNull final BlockPos block1, @NotNull final BlockPos block2, @NotNull final IBlockAccess world)
    {
        return instance.hasFittingTreeFamilyCompat(block1, block2, world);
    }
}
