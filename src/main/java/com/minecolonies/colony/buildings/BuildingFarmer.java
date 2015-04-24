package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobFarmer;
import com.minecolonies.colony.jobs.JobPlaceholder;
import net.minecraft.util.ChunkCoordinates;

public class BuildingFarmer extends BuildingWorker
{
    public BuildingFarmer(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Farmer"; }

    @Override
    public int getMaxBuildingLevel(){ return 3; }

    @Override
    public String getJobName(){ return "Farmer"; }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobFarmer(citizen); //TODO Implement Later
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow(int guiId)
        {
            return new WindowHutWorkerPlaceholder<BuildingFarmer.View>(this, "farmerHut");
        }
    }

    public int getFarmRadius()
    {
        return getBuildingLevel()+3;
    }


}
