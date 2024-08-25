package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the builder. No different from {@link AbstractBlockHut}
 */
public class BlockHutBuilder extends AbstractBlockHut<BlockHutBuilder>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.builder.get();
    }
}
