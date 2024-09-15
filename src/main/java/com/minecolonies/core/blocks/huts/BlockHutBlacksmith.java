package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the blacksmith. No different from {@link AbstractBlockHut}
 */
public class BlockHutBlacksmith extends AbstractBlockHut<BlockHutBlacksmith>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.blacksmith.get();
    }
}
