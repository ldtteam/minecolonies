package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * HutBlock for the Tavern
 */
public class BlockHutTavern extends AbstractBlockHut<com.minecolonies.core.blocks.huts.BlockHutTavern>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.tavern.get();
    }
}
