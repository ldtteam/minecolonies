package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the university. No different from {@link AbstractBlockHut}
 */
public class BlockHutUniversity extends AbstractBlockHut<BlockHutUniversity>
{
    public BlockHutUniversity()
    {
        //No different from Abstract parent
        super();
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.university.get();
    }
}
