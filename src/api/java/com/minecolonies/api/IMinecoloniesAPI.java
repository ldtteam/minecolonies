package com.minecolonies.api;

import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.IGuardTypeRegistry;
import com.minecolonies.api.entity.ai.registry.IEntityAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.coremod.colony.jobs.registry.IJobRegistry;

public interface IMinecoloniesAPI
{

    static IMinecoloniesAPI getInstance() {
        return MinecoloniesAPIProxy.getInstance();
    }

    IColonyManager getColonyManager();

    ICitizenDataManager getCitizenDataManager();

    IEntityAIRegistry getEntityAIRegistry();

    IGuardTypeRegistry getGuardTypeRegistry();

    IPathNavigateRegistry getPathNavigateRegistry();

    IJobRegistry getJobRegistry();
}
