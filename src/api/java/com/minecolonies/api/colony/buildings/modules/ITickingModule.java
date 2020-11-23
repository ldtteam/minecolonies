package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * For all modules that require colony ticks.
 */
public interface ITickingModule extends IBuildingModule
{
    /**
     * Colony tick hook.
     * @param colony the colony the tick is invoked from.
     */
    default void onColonyTick(@NotNull IColony colony) { }
}
