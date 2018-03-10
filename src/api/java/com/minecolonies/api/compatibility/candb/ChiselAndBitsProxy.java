package com.minecolonies.api.compatibility.candb;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;

/**
 * This is the fallback for when c&b is not present!
 */
public class ChiselAndBitsProxy
{
    /**
     * This is the fallback for when c&b is not present!
     *
     * @param block the block.
     * @return if the block is a c&b block.
     */
    protected boolean checkForChiselAndBitsBlock(@NotNull final Block block)
    {
        return false;
    }
}