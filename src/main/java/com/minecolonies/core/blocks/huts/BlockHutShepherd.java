package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the shepherd. No different from {@link AbstractBlockHut}
 */
public class BlockHutShepherd extends AbstractBlockHut
{
    public BlockHutShepherd(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.shepherd.get();
    }
}
