package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.GLASSBLOWER_RESEARCH;

/**
 * Hut for the glassblower. No different from {@link AbstractBlockHut}
 */
public class BlockHutGlassblower extends AbstractBlockHut<BlockHutGlassblower>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutglassblower";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.glassblower;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColonyView colony)
    {
        checkResearch(colony, GLASSBLOWER_RESEARCH);
    }
}
