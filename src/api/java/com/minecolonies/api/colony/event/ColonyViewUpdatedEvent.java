package com.minecolonies.api.colony.event;

import com.minecolonies.api.colony.IColonyView;
import org.jetbrains.annotations.NotNull;

/**
 * This event is raised client-side whenever a particular colony's data is refreshed.
 */
public class ColonyViewUpdatedEvent extends AbstractColonyEvent
{
    /**
     * Constructs a new event.
     *
     * @param colony The colony (view) that was just updated.
     */
    public ColonyViewUpdatedEvent(final @NotNull IColonyView colony)
    {
        super(colony);
    }

    @Override
    public @NotNull IColonyView getColony()
    {
        return (IColonyView) super.getColony();
    }
}
