package com.minecolonies.api.compatibility.candb;

import com.minecolonies.api.util.ItemStackUtils;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitVisitor;
import mod.chiselsandbits.api.IChiseledBlockTileEntity;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * This class is to store a check to see if a block is a chiselsandbits block.
 */
public final class ChiselAndBitsCheck extends ChiselAndBitsProxy
{
    private static final String CANDB = "chiselsandbits";

    /**
     * Check if tileEntity is c&b block.
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a c&b tileEntity.
     */
    public static boolean isChiselAndBitsTileEntity(@NotNull final TileEntity tileEntity)
    {
        return new ChiselAndBitsCheck().checkForChiselAndBitsTileEntity(tileEntity);
    }

    /**
     * Check if blockState is c&b block.
     *
     * @param blockState the blockState.
     * @return if the blockState is a c&b blockState.
     */
    public static boolean isChiselAndBitsBlock(@NotNull final IBlockState blockState)
    {
        return new ChiselAndBitsCheck().checkForChiselAndBitsBlock(blockState);
    }

    /**
     * Get c&b bits as a list of itemStacks from tileEntity..
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
        return blockState.getBlock() instanceof BlockChiseled;
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

            access.visitBits(new IBitVisitor()
            {
                @Override
                public IBitBrush visitBit(final int x, final int y, final int z, final IBitBrush iBitBrush)
                {
                    if (iBitBrush.getStateID() != 0)
                    {
                        ItemStack stack = iBitBrush.getItemStack(x);
                        for (final ItemStack tempStack : stacks)
                        {
                            if (ItemHandlerHelper.canItemStacksStack(tempStack, stack) && tempStack.getCount() < STACKSIZE)
                            {
                                final int count = STACKSIZE - tempStack.getCount();
                                if(stack.getCount() < count)
                                {
                                    tempStack.setCount(tempStack.getCount() + stack.getCount());
                                    stack = ItemStack.EMPTY;
                                }
                                else
                                {
                                    tempStack.setCount(STACKSIZE);
                                    stack.setCount(stack.getCount() - count);
                                }
                            }
                        }
                        if(!ItemStackUtils.isEmpty(stack))
                        {
                            stacks.add(stack);
                        }
                    }
                    return iBitBrush;
                }
            });

            return stacks;
        }
        return Collections.emptyList();
    }
}
