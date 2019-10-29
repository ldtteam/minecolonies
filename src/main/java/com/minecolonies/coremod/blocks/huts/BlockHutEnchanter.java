package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

public class BlockHutEnchanter extends AbstractBlockHut<BlockHutEnchanter>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutenchanter";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.enchanter;
    }
}
