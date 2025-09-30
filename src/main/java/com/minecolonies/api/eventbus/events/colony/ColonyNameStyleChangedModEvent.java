package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony name style changed event.
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
