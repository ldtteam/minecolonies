package com.minecolonies.api.colony.busevents;

import com.minecolonies.api.colony.IColony;

public class ColonyTickEvent implements IColonyEvent
{
    final IColony colony;

    public ColonyTickEvent(final IColony colony) {this.colony = colony;}

    @Override
    public IColony getColony()
    {
        return null;
    }
}
