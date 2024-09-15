package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the stone mason. No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut<BlockHutStonemason>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.stoneMason.get();
    }
}
