package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the glassblower. No different from {@link AbstractBlockHut}
 */
public class BlockHutGlassblower extends AbstractBlockHut
{
    public BlockHutGlassblower(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.glassblower.get();
    }
}
