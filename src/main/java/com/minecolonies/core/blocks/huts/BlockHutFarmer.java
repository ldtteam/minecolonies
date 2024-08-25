package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the Farmer. No different from {@link AbstractBlockHut}
 */

public class BlockHutFarmer extends AbstractBlockHut<BlockHutFarmer>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.farmer.get();
    }
}
