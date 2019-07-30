package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.coremod.colony.*;

public class MinecoloniesApiImpl implements IMinecoloniesAPI
{
    private static MinecoloniesApiImpl ourInstance = new MinecoloniesApiImpl();

    public static MinecoloniesApiImpl getInstance()
    {
        return ourInstance;
    }

    private final IColonyManager colonyManager = new ColonyManager();
    private final ICitizenDataManager citizenDataManager = new CitizenDataManager();

    @Override
    public IColonyManager getColonyManager()
    {
        return colonyManager;
    }

    @Override
    public ICitizenDataManager getCitizenDataManager()
    {
        return citizenDataManager;
    }

    private MinecoloniesApiImpl()
    {
    }
}
