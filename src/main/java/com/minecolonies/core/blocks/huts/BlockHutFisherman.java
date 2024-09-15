package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the fisherman. No different from {@link AbstractBlockHut}
 */
public class BlockHutFisherman extends AbstractBlockHut<BlockHutFisherman>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.fisherman.get();
    }
}
