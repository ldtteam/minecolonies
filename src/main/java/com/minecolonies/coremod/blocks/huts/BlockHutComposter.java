package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;

import org.jetbrains.annotations.NotNull;

public class BlockHutComposter extends AbstractBlockHut<BlockHutComposter>
{

    @NotNull
    @Override
    public String getName() {return "blockhutcomposter";}

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.composter;
    }

    @Override
    public void checkResearch(final IColony colony)
    {
        if (colony == null)
        {
            needsResearch = false;
        }
        needsResearch = colony.getResearchManager().getResearchEffects().getEffect("Composter", UnlockBuildingResearchEffect.class) == null;
    }
}
