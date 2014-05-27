package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutStonemason extends TileEntityHutWorker
{
    public TileEntityHutStonemason()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutStonemason";
    }

    @Override
    public String getJobName()
    {
        return "Stonemason";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return null;//TODO
    }
}
