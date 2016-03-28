package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobLumberjack;
import net.minecraft.util.ChunkCoordinates;

public class BuildingLumberjack extends BuildingWorker
{
    private static final String LUMBERJACK          = "Lumberjack";
    private static final String LUMBERJACK_HUT_NAME = "lumberjackHut";

    public BuildingLumberjack(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return LUMBERJACK;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public String getJobName()
    {
        return LUMBERJACK;
    }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    public static class View extends BuildingWorker.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<BuildingLumberjack.View>(this, LUMBERJACK_HUT_NAME);
        }
    }
}
