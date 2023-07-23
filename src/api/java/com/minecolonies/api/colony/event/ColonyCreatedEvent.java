package com.minecolonies.api.colony.event;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony created event.
 */
public class ColonyCreatedEvent extends AbstractColonyEvent
{
    /**
     * Constructs a colony created event.
     *
     * @param colony The colony related to the event.
     */
    public ColonyCreatedEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
