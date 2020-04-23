package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the glassblower.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutGlassblower extends AbstractBlockHut<BlockHutGlassblower>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutglassblower";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.glassblower;
    }
}
