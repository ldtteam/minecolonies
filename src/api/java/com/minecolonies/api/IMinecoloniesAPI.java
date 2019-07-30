package com.minecolonies.api;

import com.minecolonies.coremod.colony.ICitizenDataManager;
import com.minecolonies.coremod.colony.IColonyManager;

public interface IMinecoloniesAPI
{

    static IMinecoloniesAPI getInstance() {
        return MinecoloniesAPIProxy.getInstance();
    }

    IColonyManager getColonyManager();

    ICitizenDataManager getCitizenDataManager();
}
