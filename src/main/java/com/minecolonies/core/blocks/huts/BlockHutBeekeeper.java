package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the beekeeper. No different from {@link AbstractBlockHut}
 */
public class BlockHutBeekeeper extends AbstractBlockHut
{
    public BlockHutBeekeeper(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.beekeeper.get();
    }
}
