package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a colony's name pack is changed.
 */
public final class ColonyNameStyleChangedModEvent extends AbstractColonyModEvent
{
    /**
     * Constructs a colony name style changed event.
     *
     * @param colony the colony related to the event.
     */
    public ColonyNameStyleChangedModEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
