package com.minecolonies.api.compatibility.candb;

import com.minecolonies.api.crafting.ItemStorage;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitVisitor;
import mod.chiselsandbits.api.IChiseledBlockTileEntity;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.core.api.ChiselAndBitsAPI;
import mod.chiselsandbits.items.ItemChiseledBit;
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

            access.visitBits((x, y, z, iBitBrush) -> {
                if (iBitBrush.getStateID() != 0)
                {
                    stacks.add(iBitBrush.getItemStack(x));
                }
                return iBitBrush;
            });

            final Map<ItemStorage, Integer> list = new HashMap<>();
            for (final ItemStack stack : stacks)
            {
                ItemStorage tempStorage = new ItemStorage(stack.copy());
                tempStorage.setAmount(1);
                if (list.containsKey(tempStorage))
                {
                    final int oldSize = list.get(tempStorage);
                    tempStorage.setAmount(tempStorage.getAmount() + oldSize);
                }
                list.put(tempStorage, tempStorage.getAmount());
            }

            stacks.clear();
            for (final Map.Entry<ItemStorage, Integer> storage : list.entrySet())
            {
                int count = storage.getValue();
                while (count > 0)
                {
                    final ItemStack stack = storage.getKey().getItemStack().copy();
                    final int size = Math.min(stack.getMaxStackSize(), count);
                    stack.setCount(size);
                    stacks.add(stack);
                    count -= size;
                }
            }

            return stacks;
        }
        return Collections.emptyList();
    }
}
