package com.minecolonies.api.compatibility.candb;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * This class is to store a check to see if a block is a chiselsandbits block.
 */
public final class ChiselAndBitsCheck extends AbstractChiselAndBitsProxy
{
    /**
     * Check if tileEntity is candb block.
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a candb tileEntity.
     */
    public static boolean isChiselAndBitsTileEntity(@NotNull final BlockEntity tileEntity)
    {
        return new ChiselAndBitsCheck().checkForChiselAndBitsTileEntity(tileEntity);
    }

    /**
     * Check if blockState is candb block.
     *
     * @param blockState the blockState.
     * @return if the blockState is a candb blockState.
     */
    public static boolean isChiselAndBitsBlock(@NotNull final BlockState blockState)
    {
        return new ChiselAndBitsCheck().checkForChiselAndBitsBlock(blockState);
    }

    /**
     * Get candb bits as a list of itemStacks from tileEntity..
     *
     * @param tileEntity the tileEntity.
     * @return the list of itemStacks..
     */
    public static List<ItemStack> getBitStacks(final BlockEntity tileEntity)
    {
        return new ChiselAndBitsCheck().getChiseledStacks(tileEntity);
    }

    @Override
    public boolean checkForChiselAndBitsBlock(@NotNull final BlockState blockState)
    {
        return false;
    }

    @Override
    public boolean checkForChiselAndBitsTileEntity(@NotNull final BlockEntity tileEntity)
    {
        return false;
    }

    @Override
    public List<ItemStack> getChiseledStacks(@NotNull final BlockEntity tileEntity)
    {
        return Collections.emptyList();
    }
}
