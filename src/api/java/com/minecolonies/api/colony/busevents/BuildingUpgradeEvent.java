package com.minecolonies.api.colony.busevents;

import com.minecolonies.api.colony.IColony;

/**
 * Event fired on Building upgrades
 */
public class BuildingUpgradeEvent implements IColonyEvent
{
    final IColony colony;

    public BuildingUpgradeEvent(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }
}
