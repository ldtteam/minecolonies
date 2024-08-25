package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the Medium quarry. No different from {@link AbstractBlockHut}
 */
public class MediumQuarry extends AbstractBlockHut<MediumQuarry>
{
    public MediumQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.mediumQuarry.get();
    }
}
