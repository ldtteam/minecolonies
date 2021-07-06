package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the bakery. No different from {@link AbstractBlockHut}
 */
public class BlockHutBaker extends AbstractBlockHut<BlockHutBaker>
{
    public BlockHutBaker()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutbaker";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.bakery;
    }
}
