package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutBuilder extends TileEntityHutWorker
{
    public TileEntityHutBuilder()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "hutBuilder";
    }

    @Override
    public String getJobName()
    {
        return "Builder";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return new EntityBuilder(worldObj);
    }
}
