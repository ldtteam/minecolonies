package com.minecolonies.api.compatibility.candb;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * This is the fallback for when candb is not present!
 */
public abstract class AbstractChiselAndBitsProxy
{
    /**
     * This is the fallback for when candb is not present!
     *
     * @param blockState the BlockState.
     * @return if the blockState is a candb blockState.
     */
    public boolean checkForChiselAndBitsBlock(@NotNull final BlockState blockState)
    {
        return false;
    }

    /**
     * This is the fallback for when candb is not present!
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a candb tileEntity.
     */
    public boolean checkForChiselAndBitsTileEntity(@NotNull final BlockEntity tileEntity)
    {
        return false;
    }

    /**
     * Check if tileEntity is candb tileEntity.
     *
     * @param tileEntity the tileEntity.
     * @return if the tileEntity is a candb tileEntity.
     */
    public List<ItemStack> getChiseledStacks(@NotNull final BlockEntity tileEntity)
    {
        return Collections.emptyList();
    }
}