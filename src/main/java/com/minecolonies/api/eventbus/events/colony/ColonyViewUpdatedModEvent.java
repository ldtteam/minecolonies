package com.minecolonies.api.eventbus.events.colony;

import com.minecolonies.api.colony.IColonyView;
import org.jetbrains.annotations.NotNull;

/**
 * This event is raised client-side whenever a particular colony's data is refreshed.
 */
public final class ColonyViewUpdatedModEvent extends AbstractColonyModEvent
{
    /**
     * Constructs a new event.
     *
     * @param colony The colony (view) that was just updated.
     */
    public ColonyViewUpdatedModEvent(final @NotNull IColonyView colony)
    {
        super(colony);
    }

    @Override
    public @NotNull IColonyView getColony()
    {
        return (IColonyView) super.getColony();
    }
}
