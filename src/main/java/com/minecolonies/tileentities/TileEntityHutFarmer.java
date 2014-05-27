package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutFarmer extends TileEntityHutWorker
{
    public TileEntityHutFarmer()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutFarmer";
    }

    @Override
    public String getJobName()
    {
        return "Farmer";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return null;//TODO
    }
}
