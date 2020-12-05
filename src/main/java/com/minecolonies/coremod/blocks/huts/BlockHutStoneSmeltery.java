package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the StoneSmeltery. No different from {@link AbstractBlockHut}
 */
public class BlockHutStoneSmeltery extends AbstractBlockHut<BlockHutStoneSmeltery>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutstonesmeltery";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.stoneSmelter;
    }
}
