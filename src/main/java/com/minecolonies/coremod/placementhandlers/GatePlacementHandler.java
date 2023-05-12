package com.minecolonies.coremod.placementhandlers;

import com.minecolonies.api.blocks.decorative.AbstractBlockGate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Placement handler for special gate blocks
 */
public class GatePlacementHandler extends GeneralBlockPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof AbstractBlockGate;
    }
}
