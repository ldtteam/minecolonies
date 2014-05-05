package com.minecolonies.tilentities;

import com.minecolonies.entity.EntityCitizen;

import java.util.ArrayList;
import java.util.UUID;

public class TileEntityHutWorker extends TileEntityHut
{
    private String jobName = "";
    public boolean isProperWorker(EntityCitizen entityCitizen)
    {
        return entityCitizen.level.getSexInt() != 1 && !this.isHasWorker();
    }

    public void atemptToAddIdleCitizens(TileEntityHutWorker tileEntityHutWorker)
    {
        ArrayList<UUID> citizens = this.getTownHall().getCitizens();
        //TODO ATTEMP TO ADD
    }

    public String getJobName()
    {
        return jobName;
    }
}
