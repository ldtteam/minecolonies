package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Module type to register specific blocks to a building (beds, workstations, etc).
 */
public interface IRegistersBlockModule extends IBuildingModule
{
    /**
     * Attempt to register a specific block at a specific module.
     * @param blockState the state.
     * @param pos the position.
     * @param world the world.
     */
    void registerBlockPosition(@NotNull BlockState blockState, @NotNull BlockPos pos, @NotNull World world);
}
