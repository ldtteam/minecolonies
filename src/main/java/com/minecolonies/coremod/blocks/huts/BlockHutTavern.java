package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * HutBlock for the Tavern
 */
public class BlockHutTavern extends AbstractBlockHut<com.minecolonies.coremod.blocks.huts.BlockHutTavern>
{
    public static final String BLOCKHUT_TAVERN = "blockhuttavern";

    @NotNull
    @Override
    public String getName()
    {
        return BLOCKHUT_TAVERN;
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.tavern;
    }
}
