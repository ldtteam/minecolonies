package com.minecolonies.api.colony.managers.events;

import com.minecolonies.api.colony.IColonyManager;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Colony manager unloaded event.
 */
public class ColonyManagerUnloadedEvent extends Event
{
    /**
     * The colony manager instance.
     */
    @NotNull
    private final IColonyManager colonyManager;

    /**
     * Event for colony manager loaded.
     */
    public ColonyManagerUnloadedEvent(final @NotNull IColonyManager colonyManager)
    {
        this.colonyManager = colonyManager;
    }

    /**
     * Get the colony manager instance.
     *
     * @return the colony manager.
     */
    public @NotNull IColonyManager getColonyManager()
    {
        return colonyManager;
    }
}
