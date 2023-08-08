package com.minecolonies.api.colony.event;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony deleted event.
 */
public class ColonyDeletedEvent extends AbstractColonyEvent
{
    /**
     * Constructs a colony deleted event.
     *
     * @param colony The colony related to the event.
     */
    public ColonyDeletedEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
