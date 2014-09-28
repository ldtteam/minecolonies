package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.jobs.ColonyJob;
import net.minecraft.util.ChunkCoordinates;

public class BuildingFarmer extends BuildingWorker
{
    public BuildingFarmer(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName() { return "Farmer"; }

    @Override
    public String getJobName() { return "Farmer"; }

    @Override
    public Class<ColonyJob> getJobClass()
    {
        return ColonyJob.class; //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
