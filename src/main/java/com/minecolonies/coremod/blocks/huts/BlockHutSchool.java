package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the school.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutSchool extends AbstractBlockHut<BlockHutSchool>
{
    public BlockHutSchool()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutschool";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.school;
    }
}
