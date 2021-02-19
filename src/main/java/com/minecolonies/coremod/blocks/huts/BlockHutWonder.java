package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Hut for the wonder. No different from {@link AbstractBlockHut}
 */
public class BlockHutWonder extends AbstractBlockHut<BlockHutWonder>
{
    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    @Override
    public String getName()
    {
        return "blockhutwonder";
    }

    /**
     * Method to get the building registry entry.
     *
     * @return The building entry.
     */
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.wonder;
    }
}
