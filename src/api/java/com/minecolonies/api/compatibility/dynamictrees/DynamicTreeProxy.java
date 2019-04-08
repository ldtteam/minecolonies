package com.minecolonies.api.compatibility.dynamictrees;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * This is the fallback for when dynamictrees is not present!
 */
public class DynamicTreeProxy
{
    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     */
    protected boolean isDynamicTreePresent()
    {
        return false;
    }

    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @param block Block to check
     * @return false
     */
    protected boolean checkForDynamicTreeBlock(final Block block)
    {
        return false;
    }

    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @param block Block to check
     * @return false
     */
    protected boolean checkForDynamicLeavesBlock(final Block block)
    {
        return false;
    }

    /**
     * Default method for when dynamic Tree's mod is not present, returns false.
     *
     * @param block Block to check
     * @return false
     */
    protected boolean checkForDynamicTrunkShellBlock(final Block block)
    {
        return false;
    }

    /**
     * Get the list of Drops from a Dynamic leaf
     *
     * @param leaf The leaf to check
     * @return NonNullList<ItemStack> Drops
     */
    protected NonNullList<ItemStack> getDropsForLeaf(
      final IBlockAccess world,
      final BlockPos pos,
      final IBlockState blockstate,
      final int fortune,
      final Block leaf)
    {return NonNullList.create();}

    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @param item Block to check
     * @return false
     */
    protected boolean checkForDynamicSapling(final Item item)
    {
        return false;
    }

    /**
     * Default method when dynamic tree's isnt present
     *
     * @return Null
     */
    protected Runnable getTreeBreakActionCompat(final World world, final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos) {return null;}

    /**
     * Default method for trying to plant a dynamic sapling when the mod isnt present.
     *
     * @return false
     */
    protected boolean plantDynamicSaplingCompat(final World world, final BlockPos location, final ItemStack sapling) {return false;}

    /**
     * Default method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     */
    protected boolean hasFittingTreeFamilyCompat(final BlockPos block1, final BlockPos block2, final IBlockAccess world) {return false;}
}
