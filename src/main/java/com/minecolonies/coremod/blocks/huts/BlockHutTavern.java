package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.colony.buildings.modules.BedHandlingModule;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.TavernBuildingModule;
import org.jetbrains.annotations.NotNull;

/**
 * HutBlock for the Tavern
 */
public class BlockHutTavern extends AbstractBlockHut<com.minecolonies.coremod.blocks.huts.BlockHutTavern>
{
    /**
     * Block name
     */
    public static final String BLOCKHUT_TAVERN = "blockhuttavern";

    @NotNull
    @Override
    public String getName()
    {
        return BLOCKHUT_TAVERN;
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.tavern;
    }

    @Override
    public void registerBuildingModules(final IBuilding building)
    {
        building.registerModule(new BedHandlingModule(building));
        building.registerModule(new LivingBuildingModule(building));
        building.registerModule(new TavernBuildingModule(building));
    }
}
