package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.ResearchInitializer;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the dyer. No different from {@link AbstractBlockHut}
 */
public class BlockHutDyer extends AbstractBlockHut<BlockHutDyer>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutdyer";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.dyer;
    }

    @Override
    public void checkResearch(final IColony colony)
    {
        checkResearch(colony, ResearchInitializer.DYER_RESEARCH);
    }
}
