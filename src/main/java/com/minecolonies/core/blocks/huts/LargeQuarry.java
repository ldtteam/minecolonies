package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the Large quarry. No different from {@link AbstractBlockHut}
 */
public class LargeQuarry extends AbstractBlockHut<LargeQuarry>
{
    public LargeQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return null;//ModBuildings.largeQuarry;
    }
}
