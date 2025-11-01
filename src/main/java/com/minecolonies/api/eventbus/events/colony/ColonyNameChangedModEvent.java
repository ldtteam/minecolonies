package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColony;
import org.jetbrains.annotations.NotNull;

/**
 * Colony name changed event.
 */
public final class ColonyNameChangedModEvent extends AbstractColonyModEvent
{
    /**
     * Constructs a colony name changed event.
     *
     * @param colony the colony related to the event.
     */
    public ColonyNameChangedModEvent(final @NotNull IColony colony)
    {
        super(colony);
    }
}
