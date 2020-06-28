package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the plantation.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutPlantation extends AbstractBlockHut<BlockHutPlantation>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutplantation";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.plantation;
    }
}
