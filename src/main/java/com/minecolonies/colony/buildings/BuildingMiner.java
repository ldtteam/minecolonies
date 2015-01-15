package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobMiner;
import net.minecraft.util.ChunkCoordinates;

public class BuildingMiner extends BuildingWorker
{
    public BuildingMiner(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName(){ return "Miner"; }

    @Override
    public int getMaxBuildingLevel(){ return 3; }

    @Override
    public String getJobName(){ return "Miner"; }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobMiner(citizen);
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow(int guiId)
        {
            return new WindowHutWorkerPlaceholder<BuildingMiner.View>(this, "minerHut");
        }
    }
}
