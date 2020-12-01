package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.SCHOOL_RESEARCH;

/**
 * Hut for the school. No different from {@link AbstractBlockHut}
 */
public class BlockHutSchool extends AbstractBlockHut<BlockHutSchool>
{
    public BlockHutSchool()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutschool";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.school;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void checkResearch(final IColonyView colony)
    {
        checkResearch(colony, SCHOOL_RESEARCH);
    }
}
