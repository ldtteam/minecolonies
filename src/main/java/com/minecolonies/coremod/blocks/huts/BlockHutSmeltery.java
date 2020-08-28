package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Smeltery. No different from {@link AbstractBlockHut}
 */
public class BlockHutSmeltery extends AbstractBlockHut<BlockHutSmeltery>
{
    public BlockHutSmeltery()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutsmeltery";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.smeltery;
    }

    @Override
    public void checkResearch(final IColony colony)
    {
        if (colony == null)
        {
            needsResearch = false;
        }
        needsResearch = colony.getResearchManager().getResearchEffects().getEffect("Smeltery", UnlockBuildingResearchEffect.class) == null;
    }
}
