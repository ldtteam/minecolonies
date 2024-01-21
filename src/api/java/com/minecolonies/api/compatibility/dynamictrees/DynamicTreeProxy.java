package com.minecolonies.api.compatibility.dynamictrees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This is the fallback for when dynamictrees is not present!
 */
public class DynamicTreeProxy
{
    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @return true if so.
     */
    public boolean isDynamicTreePresent()
    {
        return false;
    }

    /**
     * Returns the damageType string falling dynamic Tree's use
     *
     * @return damageType
     */
    public ResourceKey<DamageType> getDynamicTreeDamage()
    {
        return null;
    }

    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @param block Block to check
     * @return false
     */
    public boolean checkForDynamicTreeBlock(final Block block)
    {
        return false;
    }

    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @param block Block to check
     * @return false
     */
    public boolean checkForDynamicLeavesBlock(final Block block)
    {
        return false;
    }

    /**
     * Default method for when dynamic Tree's mod is not present, returns false.
     *
     * @param block Block to check
     * @return false
     */
    public boolean checkForDynamicTrunkShellBlock(final Block block)
    {
        return false;
    }

    /**
     * Get the list of Drops from a Dynamic leaf
     *
     * @param leaf       The leaf to check
     * @param world      the world it is in.
     * @param pos        the pos of the block.
     * @param blockstate the blockstate to check.
     * @param fortune    the fortune effect.
     * @return {@link NonNullList} of {@link ItemStack} Drops
     */
    public NonNullList<ItemStack> getDropsForLeaf(
      final LevelAccessor world,
      final BlockPos pos,
      final BlockState blockstate,
      final int fortune,
      final Block leaf)
    {return NonNullList.create();}

    /**
     * Default method for when dynamic Tree's mod is not present, returns false
     *
     * @param item Block to check
     * @return false
     */
    public boolean checkForDynamicSapling(final Item item)
    {
        return false;
    }

    /**
     * Default method when dynamic tree's isnt present
     *
     * @param world        the world it is in.
     * @param blockToBreak the block position.
     * @param toolToUse    the tool
     * @param workerPos    the pos of the worker
     * @return Null
     */
    public Runnable getTreeBreakActionCompat(final Level world, final BlockPos blockToBreak, final ItemStack toolToUse, final BlockPos workerPos) {return null;}

    /**
     * Default method for trying to plant a dynamic sapling when the mod isnt present.
     *
     * @param world    the world it is in.
     * @param location the position.
     * @param sapling  the sapling stack.
     * @return false
     */
    public boolean plantDynamicSaplingCompat(final Level world, final BlockPos location, final ItemStack sapling) {return false;}

    /**
     * Default method to check if two given blocks have the same Tree family
     *
     * @param block1 First blockpos to compare
     * @param block2 Second blockpos to compare
     * @param world  the world it is in.
     * @return if compat exists.
     */
    public boolean hasFittingTreeFamilyCompat(final BlockPos block1, final BlockPos block2, final LevelAccessor world) {return false;}
}
