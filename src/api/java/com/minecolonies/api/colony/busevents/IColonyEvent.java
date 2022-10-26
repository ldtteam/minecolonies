package com.minecolonies.api.colony.busevents;

import com.minecolonies.api.colony.IColony;

/**
 * Basic colony event type
 */
public interface IColonyEvent
{
    /**
     * Get the colony of the event
     *
     * @return
     */
    public IColony getColony();
}
