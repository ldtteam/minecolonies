package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobPlaceholder;
import net.minecraft.util.ChunkCoordinates;

public class BuildingBaker extends BuildingWorker
{
    public BuildingBaker(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Baker"; }

    @Override
    public int getMaxBuildingLevel(){ return 3; }

    @Override
    public String getJobName() { return "Baker"; }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobPlaceholder(citizen); //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<BuildingBaker.View>(this, "bakerHut");
        }
    }
}
