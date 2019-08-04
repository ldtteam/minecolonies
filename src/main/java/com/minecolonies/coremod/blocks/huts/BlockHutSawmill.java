package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the sawmill.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutSawmill extends AbstractBlockHut<BlockHutSawmill>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockHutSawmill";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.sawmill;
    }
}
