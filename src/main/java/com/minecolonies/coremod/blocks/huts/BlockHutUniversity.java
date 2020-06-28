package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the university.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutUniversity extends AbstractBlockHut<BlockHutUniversity>
{
    public BlockHutUniversity()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutuniversity";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.university;
    }
}
