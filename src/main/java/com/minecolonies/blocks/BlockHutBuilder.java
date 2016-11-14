package com.minecolonies.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the builder.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBuilder extends AbstractBlockHut
{
    protected BlockHutBuilder()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBuilder";
    }

}
