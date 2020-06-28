package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the hospital.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutHospital extends AbstractBlockHut<BlockHutHospital>
{
    public BlockHutHospital()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhuthospital";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.hospital;
    }
}
