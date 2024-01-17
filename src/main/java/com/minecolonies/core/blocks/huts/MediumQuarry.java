package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Medium quarry. No different from {@link AbstractBlockHut}
 */
public class MediumQuarry extends AbstractBlockHut<MediumQuarry>
{
    public MediumQuarry()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getHutName()
    {
        return ModBuildings.MEDIUM_QUARRY_ID;
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.mediumQuarry.get();
    }
}
