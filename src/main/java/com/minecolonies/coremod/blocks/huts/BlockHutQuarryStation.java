package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Quarry.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutQuarryStation extends AbstractBlockHut<BlockHutQuarryStation>
{
    public BlockHutQuarryStation()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutquarrystation";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.quarryStation;
    }
}
