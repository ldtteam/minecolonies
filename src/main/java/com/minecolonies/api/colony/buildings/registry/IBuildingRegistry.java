package com.minecolonies.api.colony.buildings.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.neoforged.neoforge.registries.IForgeRegistry;

public interface IBuildingRegistry
{

    static IForgeRegistry<BuildingEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getBuildingRegistry();
    }
}
