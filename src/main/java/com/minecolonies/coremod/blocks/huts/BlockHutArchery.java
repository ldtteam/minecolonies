package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Block of the Archers trainings camp.
 */
public class BlockHutArchery extends AbstractBlockHut<BlockHutArchery>
{
    /**
     * Default constructor.
     */
    public BlockHutArchery()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutarchery";
    }

    /**
     * This returns a complete list of items dropped from this block.
     * @deprecated but we still need it.
     * @param world   The current world
     * @param pos     Block position in world
     * @param state   Current state
     * @param fortune Breakers fortune level
     * @return A ArrayList containing all items this block drops
     */
    @NotNull
    @Override
    @Deprecated
    public List<ItemStack> getDrops(@NotNull final IBlockAccess world, @NotNull final BlockPos pos, @NotNull final IBlockState state, final int fortune)
    {
        return Collections.emptyList();
    }
}
