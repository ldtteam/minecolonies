package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the plantation. No different from {@link AbstractBlockHut}
 */

public class BlockHutPlantation extends AbstractBlockHut<BlockHutPlantation>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.plantation.get();
    }
}
