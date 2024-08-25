package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Alchemist hut block.
 */
public class BlockHutAlchemist extends AbstractBlockHut<BlockHutAlchemist>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.alchemist.get();
    }
}
