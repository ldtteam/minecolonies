package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.colony.buildings.modules.BedHandlingModule;
import com.minecolonies.coremod.colony.buildings.modules.HomeBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the citizen. No different from {@link AbstractBlockHut}
 */
public class BlockHutCitizen extends AbstractBlockHut<BlockHutCitizen>
{
    public BlockHutCitizen()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutcitizen";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.home;
    }
}
