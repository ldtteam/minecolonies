package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.research.ResearchInitializer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the blacksmith. No different from {@link AbstractBlockHut}
 */
public class BlockHutBlacksmith extends AbstractBlockHut<BlockHutBlacksmith>
{
    public BlockHutBlacksmith()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutblacksmith";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.blacksmith;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColonyView colony)
    {
        checkResearch(colony, ResearchInitializer.BLACKSMITH_RESEARCH);
    }
}
