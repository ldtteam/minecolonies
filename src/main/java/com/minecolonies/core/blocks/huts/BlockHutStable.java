package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the stable. No different from {@link AbstractBlockHut}
 */
public class BlockHutStable extends AbstractBlockHut<BlockHutStable>
{
    //No different from Abstract parent
    private static final String BLOCK_HUT_STABLE = "blockhutstable";

    public BlockHutStable()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return BLOCK_HUT_STABLE;
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.stable.get();
    }
}
