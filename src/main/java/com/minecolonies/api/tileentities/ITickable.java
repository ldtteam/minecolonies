package com.minecolonies.api.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Interface for tickable things.
 */
public interface ITickable
{
    /**
     * Tick the tickable with parameters.
     * @param level the world its ticking in.
     * @param state its state.
     * @param pos the position its ticking at.
     */
    default void tick(final Level level, final BlockState state, final BlockPos pos)
    {
        tick();
    }

    /**
     * Default parameterless ticking implementation.
     */
    default void tick()
    {

    }
}
