package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the rabbit hutch. No different from {@link AbstractBlockHut}
 */
public class BlockHutRabbitHutch extends AbstractBlockHut
{
    public BlockHutRabbitHutch(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.rabbitHutch.get();
    }
}
