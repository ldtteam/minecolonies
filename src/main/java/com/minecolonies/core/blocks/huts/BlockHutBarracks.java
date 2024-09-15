package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Block of the Barracks.
 */
public class BlockHutBarracks extends AbstractBlockHut<BlockHutBarracks>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.barracks.get();
    }
}
