package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Block of the Archers trainings camp.
 */
public class BlockHutArchery extends AbstractBlockHut<BlockHutArchery>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.archery.get();
    }
}
