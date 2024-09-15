package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Block of the GuardTower hut.
 */
public class BlockHutGuardTower extends AbstractBlockHut<BlockHutGuardTower>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.guardTower.get();
    }
}
