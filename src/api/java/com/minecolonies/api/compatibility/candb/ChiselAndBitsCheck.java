package com.minecolonies.api.compatibility.candb;

import mod.chiselsandbits.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
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
    public static boolean isChiselAndBitsBlock(@NotNull final IBlockState blockState)
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
    @Optional.Method(modid = CANDB)
    public boolean checkForChiselAndBitsBlock(@NotNull final IBlockState blockState)
    {
        return blockState.getBlock() instanceof IMultiStateBlock;
    }

    @Override
    @Optional.Method(modid = CANDB)
    public boolean checkForChiselAndBitsTileEntity(@NotNull final TileEntity tileEntity)
    {
        return tileEntity instanceof IChiseledBlockTileEntity;
    }

    @Override
    @Optional.Method(modid = CANDB)
    public List<ItemStack> getChiseledStacks(@NotNull final TileEntity tileEntity)
    {
        if (tileEntity instanceof IChiseledBlockTileEntity)
        {
            final IBitAccess access = ((IChiseledBlockTileEntity) tileEntity).getBitAccess();
            final List<ItemStack> stacks = new ArrayList<>();

            access.getStateCounts().forEach(stateCount ->
            {
                if (stateCount.stateId == 0)
                {
                    return;
                }

                final ItemStack bitStack = ChiselsAndBitsAPI.getBitStack(stateCount.stateId);
                if (bitStack.isEmpty())
                {
                    return;
                }

                int count = stateCount.quantity;
                final int max = bitStack.getMaxStackSize();
                while (count > max)
                {
                    final ItemStack copy = bitStack.copy();
                    copy.setCount(max);
                    stacks.add(copy);
                    count -= max;
                }
                if (count > 0)
                {
                    bitStack.setCount(count);
                    stacks.add(bitStack);
                }
            });
            return stacks;
        }
        return Collections.emptyList();
    }
}
