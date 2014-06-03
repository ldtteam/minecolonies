package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutBlacksmith extends TileEntityHutWorker
{
    public TileEntityHutBlacksmith()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutBlacksmith";
    }

    @Override
    public String getJobName()
    {
        return "Blacksmith";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return new EntityCitizen(worldObj); //TODO Implement Later
    }
}
