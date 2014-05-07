package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutWorker extends TileEntityHut
{
    private String jobName = "";
    public boolean isProperWorker(EntityCitizen entityCitizen)
    {
        return entityCitizen.level.getSexInt() != 1 && !this.isHasWorker();
    }

    public String getJobName()
    {
        return jobName;
    }
}
