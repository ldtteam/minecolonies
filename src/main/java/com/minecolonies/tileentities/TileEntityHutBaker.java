package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutBaker extends TileEntityHutWorker
{
    public TileEntityHutBaker()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutBaker";
    }

    @Override
    public String getJobName()
    {
        return "Baker";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return new EntityCitizen(worldObj); //TODO Implement Later
    }
}
