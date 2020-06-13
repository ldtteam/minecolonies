package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the rabbit hutch.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutRabbitHutch extends AbstractBlockHut<BlockHutRabbitHutch>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutrabbithutch";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.rabbitHutch;
    }
}
