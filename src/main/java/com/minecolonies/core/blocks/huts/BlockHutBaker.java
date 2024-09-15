package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the bakery. No different from {@link AbstractBlockHut}
 */
public class BlockHutBaker extends AbstractBlockHut<BlockHutBaker>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.bakery.get();
    }
}
