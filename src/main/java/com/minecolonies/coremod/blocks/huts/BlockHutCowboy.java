package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the shepherd.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutCowboy extends AbstractBlockHut<BlockHutCowboy>
{
    public BlockHutCowboy()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutcowboy";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.cowboy;
    }
}
