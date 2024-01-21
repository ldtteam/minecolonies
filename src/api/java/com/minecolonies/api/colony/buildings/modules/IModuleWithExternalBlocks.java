package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Module type to register specific blocks to a building (beds, workstations, etc).
 */
public interface IModuleWithExternalBlocks extends IBuildingModule
{
    /**
     * Attempt to register a specific block at a specific module.
     * @param blockState the state.
     * @param pos the position.
     * @param world the world.
     */
    void onBlockPlacedInBuilding(@NotNull BlockState blockState, @NotNull BlockPos pos, @NotNull Level world);

    /**
     * Get the list of registered blocks.
     * @return the list of positions of the blocks.
     */
    List<BlockPos> getRegisteredBlocks();
}
