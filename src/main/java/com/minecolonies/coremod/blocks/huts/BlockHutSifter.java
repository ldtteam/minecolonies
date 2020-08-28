package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the sifter. No different from {@link AbstractBlockHut}
 */
public class BlockHutSifter extends AbstractBlockHut<BlockHutSifter>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutsifter";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.sifter;
    }

    @Override
    public void checkResearch(final IColony colony)
    {
        if (colony == null)
        {
            needsResearch = false;
        }
        needsResearch = colony.getResearchManager().getResearchEffects().getEffect("Sifter", UnlockBuildingResearchEffect.class) == null;
    }
}
