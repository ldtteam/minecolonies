package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the library. No different from {@link AbstractBlockHut}
 */
public class BlockHutLibrary extends AbstractBlockHut<BlockHutLibrary>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.library.get();
    }
}
