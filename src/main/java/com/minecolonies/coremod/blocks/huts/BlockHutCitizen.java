package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleProvider;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.colony.buildings.modules.HomeBuildingModule;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the citizen. No different from {@link AbstractBlockHut}
 */
public class BlockHutCitizen extends AbstractBlockHut<BlockHutCitizen> implements IBuildingModuleProvider
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

    @Override
    public void registerBuildingModules(final IBuilding building)
    {
        building.registerModule(new HomeBuildingModule(building));
    }
}
