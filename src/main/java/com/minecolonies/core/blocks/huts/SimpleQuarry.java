package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the simple quarry. No different from {@link AbstractBlockHut}
 */
public class SimpleQuarry extends AbstractBlockHut<SimpleQuarry>
{
    public SimpleQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.simpleQuarry.get();
    }
}
