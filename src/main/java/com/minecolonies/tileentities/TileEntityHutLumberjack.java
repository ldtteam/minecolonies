package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutLumberjack extends TileEntityHutWorker
{
    public TileEntityHutLumberjack()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutLumberjack";
    }

    @Override
    public String getJobName()
    {
        return "Lumberjack";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return null;//TODO
    }
}
