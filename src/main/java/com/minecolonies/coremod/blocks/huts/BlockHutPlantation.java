package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_RESEARCH;

/**
 * Hut for the plantation. No different from {@link AbstractBlockHut}
 */

public class BlockHutPlantation extends AbstractBlockHut<BlockHutPlantation>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutplantation";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.plantation;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColonyView colony)
    {
        checkResearch(colony, PLANTATION_RESEARCH);
    }
}
