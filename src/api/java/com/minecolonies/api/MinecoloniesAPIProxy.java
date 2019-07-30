package com.minecolonies.api;

import com.minecolonies.coremod.colony.ICitizenDataManager;
import com.minecolonies.coremod.colony.IColonyManager;

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
}
