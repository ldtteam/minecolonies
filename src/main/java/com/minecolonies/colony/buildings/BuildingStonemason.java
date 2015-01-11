package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobPlaceholder;
import net.minecraft.util.ChunkCoordinates;

public class BuildingStonemason extends BuildingWorker
{
    public BuildingStonemason(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Stonemason"; }

    @Override
    public int getMaxBuildingLevel(){ return 1; }

    @Override
    public String getJobName(){ return "Stonemason"; }

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

        public com.blockout.views.Window getWindow(int guiId)
        {
            return new WindowHutWorkerPlaceholder<BuildingStonemason.View>(this, "stonemasonHut");
        }
    }
}
