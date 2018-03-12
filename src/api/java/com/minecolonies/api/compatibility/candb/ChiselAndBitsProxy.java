package com.minecolonies.api.compatibility.candb;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * This is the fallback for when c&b is not present!
 */
public abstract class ChiselAndBitsProxy
{
    /**
     * This is the fallback for when c&b is not present!
     *
     * @param blockState the IBlockState.
     * @return if the blockState is a c&b blockState.
     */
    public abstract boolean checkForChiselAndBitsBlock(@NotNull final IBlockState blockState);

    /**
     * This is the fallback for when c&b is not present!
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a c&b tileEntity.
     */
    public boolean checkForChiselAndBitsTileEntity(@NotNull final TileEntity tileEntity)
    {
        return false;
    }

    /**
     * Check if tileEntity is c&b tileEntity.
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a c&b tileEntity.
     */
    public List<ItemStack> getChiseledStacks(@NotNull final TileEntity tileEntity)
    {
        return Collections.emptyList();
    }
}