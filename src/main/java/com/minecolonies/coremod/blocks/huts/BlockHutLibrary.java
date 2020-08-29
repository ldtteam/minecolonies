package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.ResearchInitializer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the library. No different from {@link AbstractBlockHut}
 */
public class BlockHutLibrary extends AbstractBlockHut<BlockHutLibrary>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutlibrary";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.library;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColony colony)
    {
        checkResearch(colony, ResearchInitializer.LIBRARY_RESEARCH);
    }
}
