package com.minecolonies.api.colony.buildings.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import net.minecraftforge.registries.IForgeRegistry;

public interface IBuildingRegistry
{

    static IForgeRegistry<BuildingEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getBuildingRegistry();
    }
}
