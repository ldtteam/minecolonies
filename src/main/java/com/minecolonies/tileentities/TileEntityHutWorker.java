package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public abstract class TileEntityHutWorker extends TileEntityHut
{
    private String jobName = "";

    public String getJobName()
    {
        return jobName;
    }

    public EntityCitizen createWorker()
    {
        return null;
    }

    public void bindWorker(EntityCitizen worker)
    {
        this.setHasWorker(true);
        worker.setWorkHut(this);
    }
}
