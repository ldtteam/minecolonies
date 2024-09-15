package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the warehouse. No different from {@link AbstractBlockHut}
 */

public class BlockHutDeliveryman extends AbstractBlockHut<BlockHutDeliveryman>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.deliveryman.get();
    }
}
