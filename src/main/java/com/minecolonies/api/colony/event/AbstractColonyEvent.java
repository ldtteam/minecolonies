package com.minecolonies.api.colony.event;

import com.minecolonies.api.colony.IColony;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This is a colony-related event in the Forge sense, not in the
 * {@link com.minecolonies.api.colony.colonyEvents.IColonyEvent} sense.
 */
public abstract class AbstractColonyEvent extends Event
{
    /**
     * The colony this event was called in.
     */
    @NotNull
    private final IColony colony;

    /**
     * Constructs a colony-based event.
     *
     * @param colony The colony related to the event.
     */
    protected AbstractColonyEvent(@NotNull final IColony colony)
    {
        this.colony = colony;
    }

    /**
     * Gets the colony related to the event.
     */
    @NotNull
    public IColony getColony()
    {
        return colony;
    }
}
