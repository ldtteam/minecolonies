package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the library.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutLibrary extends AbstractBlockHut<BlockHutLibrary>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockhutlibrary";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.library;
    }
}
