package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.jobs.ColonyJob;
import com.minecolonies.entity.jobs.JobBuilder;
import net.minecraft.util.ChunkCoordinates;

public class BuildingBuilder extends BuildingWorker
{
    public BuildingBuilder(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public String getJobName() { return "Builder"; }

    public Class<JobBuilder> getJobClass()
    {
        return JobBuilder.class; //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
