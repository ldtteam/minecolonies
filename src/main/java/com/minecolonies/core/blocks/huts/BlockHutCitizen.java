package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the citizen. No different from {@link AbstractBlockHut}
 */
public class BlockHutCitizen extends AbstractBlockHut<BlockHutCitizen>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.home.get();
    }
}
