package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the shepherd. No different from {@link AbstractBlockHut}
 */
public class BlockHutCowboy extends AbstractBlockHut<BlockHutCowboy>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.cowboy.get();
    }
}
