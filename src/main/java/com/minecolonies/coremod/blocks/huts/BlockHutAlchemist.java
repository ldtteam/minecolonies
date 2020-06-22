package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Block of the Alchemist hut.
 */
public class BlockHutAlchemist extends AbstractBlockHut<BlockHutAlchemist>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutalchemist";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.alchemist;
    }
}
