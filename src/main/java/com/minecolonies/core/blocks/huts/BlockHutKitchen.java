package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the kitchen. No different from {@link AbstractBlockHut}
 */
public class BlockHutKitchen extends AbstractBlockHut<BlockHutKitchen>
{
    public BlockHutKitchen()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutkitchen";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.kitchen.get();
    }
}
