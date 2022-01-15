package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the simple quarry. No different from {@link AbstractBlockHut}
 */
public class SimpleQuarry extends AbstractBlockHut<SimpleQuarry>
{
    public SimpleQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return "simplequarry";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.simpleQuarry;
    }
}
