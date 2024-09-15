package com.minecolonies.core.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

/**
 * Block of the combat academy camp.
 */
public class BlockHutCombatAcademy extends AbstractBlockHut<BlockHutCombatAcademy>
{
    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.combatAcademy.get();
    }
}
