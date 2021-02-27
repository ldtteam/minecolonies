package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.ResearchInitializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Hut for the mystical site. No different from {@link AbstractBlockHut}
 */
public class BlockHutMysticalSite extends AbstractBlockHut<BlockHutMysticalSite>
{
    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    @Override
    public String getName()
    {
        return "blockhutmysticalsite";
    }

    /**
     * Method to get the building registry entry.
     *
     * @return The building entry.
     */
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.mysticalSite;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColonyView colony)
    {
        checkResearch(colony, ResearchInitializer.MYSTICAL_SITE_RESEARCH);
    }
}
