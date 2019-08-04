package com.minecolonies.api;

import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.IGuardTypeRegistry;
import com.minecolonies.api.entity.ai.registry.IEntityAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.coremod.colony.jobs.registry.IJobRegistry;

public class MinecoloniesAPIProxy implements IMinecoloniesAPI
{
    private static MinecoloniesAPIProxy ourInstance = new MinecoloniesAPIProxy();

    public static MinecoloniesAPIProxy getInstance()
    {
        return ourInstance;
    }

    private MinecoloniesAPIProxy()
    {
    }

    private IMinecoloniesAPI apiInstance;

    public void setApiInstance(final IMinecoloniesAPI apiInstance)
    {
        this.apiInstance = apiInstance;
    }

    @Override
    public IColonyManager getColonyManager()
    {
        return apiInstance.getColonyManager();
    }

    @Override
    public ICitizenDataManager getCitizenDataManager()
    {
        return apiInstance.getCitizenDataManager();
    }

    @Override
    public IEntityAIRegistry getEntityAIRegistry()
    {
        return apiInstance.getEntityAIRegistry();
    }

    @Override
    public IGuardTypeRegistry getGuardTypeRegistry()
    {
        return apiInstance.getGuardTypeRegistry();
    }

    @Override
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return apiInstance.getPathNavigateRegistry();
    }

    @Override
    public IJobRegistry getJobRegistry()
    {
        return apiInstance.getJobRegistry();
    }
}
