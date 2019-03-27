package com.minecolonies.api.compatibility;

import com.minecolonies.api.compatibility.dynamictrees.DynamicTreeCompat;
import com.minecolonies.api.compatibility.tinkers.SlimeTreeCheck;
import com.minecolonies.api.compatibility.tinkers.TinkersWeaponHelper;
import com.minecolonies.api.compatibility.tinkers.ToolBrokenCheck;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.HARVESTCRAFTMODID;

/**
 * This class is to store the methods that call the methods to check for miscellaneous compatibility problems.
 */
public final class Compatibility
{

    private Compatibility()
    {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * This method checks to see if STACK is able to mine anything.
     * It goes through all compatibility checks.
     *
     * @param stack the item in question.
     * @param tool  the name of the tool.
     * @return boolean whether the stack can mine or not.
     */
    public static boolean getMiningLevelCompatibility(@Nullable final ItemStack stack, @Nullable final String tool)
    {
        return !ToolBrokenCheck.checkTinkersBroken(stack);
    }

    /**
     * This method checks if block is slime block.
     *
     * @param block the block.
     * @return if the block is a slime block.
     */
    public static boolean isSlimeBlock(@NotNull final Block block)
    {
        return SlimeTreeCheck.isSlimeBlock(block);
    }

    /**
     * This method checks if block is slime leaf.
     *
     * @param block the block.
     * @return if the block is a slime leaf.
     */
    public static boolean isSlimeLeaf(@NotNull final Block block)
    {
        return SlimeTreeCheck.isSlimeLeaf(block);
    }

    /**
     * This method checks if block is slime sapling.
     *
     * @param block the block.
     * @return if the block is a slime sapling.
     */
    public static boolean isSlimeSapling(@NotNull final Block block)
    {
        return SlimeTreeCheck.isSlimeSapling(block);
    }

    /**
     * This method checks if block is slime dirt.
     *
     * @param block the block.
     * @return if the block is slime dirt.
     */
    public static boolean isSlimeDirtOrGrass(@NotNull final Block block)
    {
        return SlimeTreeCheck.isSlimeDirtOrGrass(block);
    }

    /**
     * Get the Slime leaf variant.
     *
     * @param leaf the leaf.
     * @return the variant.
     */
    public static int getLeafVariant(@NotNull final IBlockState leaf)
    {
        return SlimeTreeCheck.getLeafVariant(leaf);
    }

    /**
     * Check if a certain itemstack is a tinkers weapon.
     *
     * @param stack the stack to check for.
     * @return true if so.
     */
    public static boolean isTinkersWeapon(@NotNull final ItemStack stack)
    {
        return TinkersWeaponHelper.isTinkersSword(stack);
    }

    /**
     * Calculate the actual attack damage of the tinkers weapon.
     *
     * @param stack the stack.
     * @return the attack damage.
     */
    public static double getAttackDamage(@NotNull final ItemStack stack)
    {
        return TinkersWeaponHelper.getDamage(stack);
    }

    /**
     * Calculate the tool level of the stack.
     *
     * @param stack the stack.
     * @return the tool level
     */
    public static int getToolLevel(@NotNull final ItemStack stack)
    {
        return TinkersWeaponHelper.getToolLvl(stack);
    }

    /**
     * Check if Pams harvestcraft is installed.
     *
     * @return true if so.
     */
    public static boolean isPamsInstalled()
    {
        return Loader.isModLoaded(HARVESTCRAFTMODID);
    }

    /**
     * Check if dynamic tree's is present
     */
    public static boolean isDynTreePresent()
    {
        return DynamicTreeCompat.isDynTreePresent();
    }

    /**
     * Returns the damageType string falling dynamic Tree's use
     *
     * @return damageType
     */
    public static String getDynamicTreeDamage()
    {
        return DynamicTreeCompat.getDynamicTreeDamage();
    }

    /**
     * Check if block is a Dynamic tree
     */
    public static boolean isDynamicBlock(final Block block)
    {
        return DynamicTreeCompat.isDynamicTreeBlock(block);
    }

    /**
     * Check if block is a Dynamic Leaf
     */
    public static boolean isDynamicLeaf(final Block block)
    {
        return DynamicTreeCompat.isDynamicLeavesBlock(block);
    }

    /**
     * Check whether the block is a shell block.
     *
     * @param block the block to check
     * @return true if it is a shell block.
     */
    public static boolean isDynamicTrunkShell(final Block block)
    {
        return DynamicTreeCompat.isDynamicTrunkShellBlock(block);
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
    public static NonNullList<ItemStack> getDropsForDynamicLeaf(final IBlockAccess world, final BlockPos pos, final IBlockState blockState, final int fortune, final Block leaf)
    {
        return DynamicTreeCompat.getDropsForLeafCompat(world, pos, blockState, fortune, leaf);
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
        return DynamicTreeCompat.plantDynamicSapling(world, location, sapling);
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
    public static Runnable getDynamicTreeBreakAction(final World world, final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos)
    {
        return DynamicTreeCompat.getTreeBreakAction(world, blockToBreak, toolToUse, workerPos);
    }

    /**
     * Check wether the item is a dynamic Sapling
     *
     * @param item Item to check
     */
    public static boolean isDynamicTreeSapling(final Item item)
    {
        return DynamicTreeCompat.isDynamicTreeSapling(item);
    }

    /**
     * Check wether the Itemstack is a dynamic Sapling
     *
     * @param stack Itemstack to check
     * @return true if it is a dynamic Sapling
     */
    public static boolean isDynamicTreeSapling(final ItemStack stack)
    {
        return DynamicTreeCompat.isDynamicTreeSapling(stack.getItem());
    }

    /**
     * Method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @return true when same family
     */
    public static boolean isDynamicFamilyFitting(final BlockPos block1, final BlockPos block2, final IBlockAccess world)
    {
        return DynamicTreeCompat.hasFittingTreeFamily(block1, block2, world);
    }
}
