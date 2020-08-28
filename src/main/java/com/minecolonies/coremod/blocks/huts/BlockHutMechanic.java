package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the mechanic. No different from {@link AbstractBlockHut}
 */
public class BlockHutMechanic extends AbstractBlockHut<BlockHutMechanic>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutmechanic";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.mechanic;
    }

    @Override
    public boolean checkResearch(final IColony colony)
    {
        if (colony == null)
        {
            return false;
        }
        return colony.getResearchManager().getResearchEffects().getEffect("Mechanic", UnlockBuildingResearchEffect.class) == null;
    }
}
