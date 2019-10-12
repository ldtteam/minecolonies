package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the PostBox.
 * No different from {@link AbstractBlockHut}
 */
public class BlockPostBox extends AbstractBlockHut<BlockPostBox>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockpostbox";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.postBox;
    }
}
