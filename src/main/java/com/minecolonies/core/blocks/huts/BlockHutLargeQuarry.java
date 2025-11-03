package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the Large quarry. No different from {@link AbstractBlockHut}
 */
public class BlockHutLargeQuarry extends AbstractBlockHut
{
    public BlockHutLargeQuarry(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return null;//ModBuildings.largeQuarry;
    }
}
