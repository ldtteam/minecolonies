package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutMiner extends TileEntityHutWorker
{
    public TileEntityHutMiner()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutMiner";
    }

    @Override
    public String getJobName()
    {
        return "Miner";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return new EntityCitizen(worldObj); //TODO Implement Later
    }
}
