package com.minecolonies.coremod.placementhandlers;

import com.minecolonies.api.blocks.decorative.AbstractBlockGate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Placement handler for special gate blocks
 */
public class GatePlacementHandler extends GeneralBlockPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof AbstractBlockGate;
    }
}
