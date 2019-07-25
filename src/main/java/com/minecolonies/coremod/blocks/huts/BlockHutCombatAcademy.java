package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Block of the combat academy camp.
 */
public class BlockHutCombatAcademy extends AbstractBlockHut<BlockHutCombatAcademy>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockHutCombatAcademy";
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
    public List<ItemStack> getDrops(@NotNull final IBlockAccess world, @NotNull final BlockPos pos, @NotNull final BlockState state, final int fortune)
    {
        return Collections.emptyList();
    }
}
