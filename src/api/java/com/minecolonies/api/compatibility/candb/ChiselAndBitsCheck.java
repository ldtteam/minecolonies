package com.minecolonies.api.compatibility.candb;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is to store a check to see if a block is a chiselsandbits block.
 */
public final class ChiselAndBitsCheck extends AbstractChiselAndBitsProxy
{
    private static final String CANDB = "chiselsandbits";

    /**
     * Check if tileEntity is candb block.
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a candb tileEntity.
     */
    public static boolean isChiselAndBitsTileEntity(@NotNull final TileEntity tileEntity)
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
    public static List<ItemStack> getBitStacks(final TileEntity tileEntity)
    {
        return new ChiselAndBitsCheck().getChiseledStacks(tileEntity);
    }

    @Override
    public boolean checkForChiselAndBitsBlock(@NotNull final BlockState blockState)
    {
        return false;
    }

    @Override
    public boolean checkForChiselAndBitsTileEntity(@NotNull final TileEntity tileEntity)
    {
        return false;
    }

    @Override
    public List<ItemStack> getChiseledStacks(@NotNull final TileEntity tileEntity)
    {
        return Collections.emptyList();
    }
}
