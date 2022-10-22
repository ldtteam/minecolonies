package com.minecolonies.api.colony.busevents;

import com.minecolonies.api.colony.IColony;

/**
 * Posted when the colonies state changes
 */
public class ColonyStateEvent implements IColonyStateEvent
{
    private final IColony colony;
    private final boolean active;

    public ColonyStateEvent(final IColony colony, final boolean active)
    {
        this.colony = colony;
        this.active = active;
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public boolean isColonyActive()
    {
        return active;
    }
}
