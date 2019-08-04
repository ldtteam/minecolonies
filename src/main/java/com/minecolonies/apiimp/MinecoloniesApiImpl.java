package com.minecolonies.apiimp;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenDataManager;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.registry.IGuardTypeRegistry;
import com.minecolonies.api.entity.ai.registry.IEntityAIRegistry;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.coremod.colony.CitizenDataManager;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.registry.GuardTypeRegistry;
import com.minecolonies.coremod.colony.jobs.registry.IJobRegistry;
import com.minecolonies.coremod.colony.jobs.registry.JobRegistry;
import com.minecolonies.coremod.entity.ai.registry.EntityAiRegistry;
import com.minecolonies.coremod.entity.pathfinding.registry.PathNavigateRegistry;

public class MinecoloniesApiImpl implements IMinecoloniesAPI
{
    private static MinecoloniesApiImpl ourInstance = new MinecoloniesApiImpl();

    public static MinecoloniesApiImpl getInstance()
    {
        return ourInstance;
    }

    private final IColonyManager        colonyManager        = new ColonyManager();
    private final ICitizenDataManager   citizenDataManager   = new CitizenDataManager();
    private final IEntityAIRegistry     entityAIRegistry     = new EntityAiRegistry();
    private final IGuardTypeRegistry    guardTypeRegistry    = new GuardTypeRegistry();
    private final IPathNavigateRegistry pathNavigateRegistry = new PathNavigateRegistry();
    private final IJobRegistry          jobRegistry          = new JobRegistry();

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

    @Override
    public IEntityAIRegistry getEntityAIRegistry()
    {
        return entityAIRegistry;
    }

    @Override
    public IGuardTypeRegistry getGuardTypeRegistry()
    {
        return guardTypeRegistry;
    }

    @Override
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return pathNavigateRegistry;
    }

    @Override
    public IJobRegistry getJobRegistry()
    {
        return jobRegistry;
    }

    private MinecoloniesApiImpl()
    {
    }
}

