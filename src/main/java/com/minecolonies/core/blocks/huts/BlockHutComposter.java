package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

public class BlockHutComposter extends AbstractBlockHut<BlockHutComposter>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.composter.get();
    }
}
