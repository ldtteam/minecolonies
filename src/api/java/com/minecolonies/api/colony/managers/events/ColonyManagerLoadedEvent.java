package com.minecolonies.api.colony.managers.events;

import com.minecolonies.api.colony.IColonyManager;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Colony manager loaded event.
 */
public final class ColonyManagerLoadedEvent extends Event
{
    /**
     * The colony manager instance.
     */
    @NotNull
    private final IColonyManager colonyManager;

    /**
     * Event for colony manager loaded.
     */
    public ColonyManagerLoadedEvent(final @NotNull IColonyManager colonyManager)
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
