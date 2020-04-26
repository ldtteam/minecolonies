package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Quarry.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutQuarry extends AbstractBlockHut<BlockHutQuarry>
{
    public BlockHutQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutquarry";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.quarry;
    }
}
