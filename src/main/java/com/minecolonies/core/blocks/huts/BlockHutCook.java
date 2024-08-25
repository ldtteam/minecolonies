package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the lumberjack. No different from {@link AbstractBlockHut}
 */
public class BlockHutCook extends AbstractBlockHut<BlockHutCook>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.cook.get();
    }
}
