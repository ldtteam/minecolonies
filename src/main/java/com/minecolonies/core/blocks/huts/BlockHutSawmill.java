package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the sawmill. No different from {@link AbstractBlockHut}
 */
public class BlockHutSawmill extends AbstractBlockHut<BlockHutSawmill>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutsawmill";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.sawmill.get();
    }
}
