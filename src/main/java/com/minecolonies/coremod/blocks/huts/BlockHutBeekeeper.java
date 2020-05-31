package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

public class BlockHutBeekeeper extends AbstractBlockHut<BlockHutBeekeeper> {
    /**
     * Method to return the name of the block.
     *
     * @return Name of the block.
     */
    @Override
    public String getName() {
        return "blockHutBeekeeper";
    }

    /**
     * Method to get the building registry entry.
     *
     * @return The building entry.
     */
    @Override
    public BuildingEntry getBuildingEntry() {
        return ModBuildings.beekeeper;
    }
}
