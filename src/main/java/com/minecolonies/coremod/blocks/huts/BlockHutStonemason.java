package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.ResearchInitializer;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the stone mason. No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut<BlockHutStonemason>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutstonemason";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.stoneMason;
    }

    @Override
    public void checkResearch(final IColony colony)
    {
        checkResearch(colony, ResearchInitializer.STONEMASON_RESEARCH);
    }
}
