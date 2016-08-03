package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobPlaceholder;
import net.minecraft.util.BlockPos;

public class BuildingBlacksmith extends AbstractBuildingWorker
{
    private static final String BLACKSMITH          = "Blacksmith";
    private static final String BLACKSMITH_HUT_NAME = "blacksmithHut";

    public BuildingBlacksmith(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return BLACKSMITH;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public String getJobName()
    {
        return BLACKSMITH;
    }

    @Override
    public Job createJob(CitizenData citizen)
    {
        return new JobPlaceholder(citizen); //TODO Implement Later
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<AbstractBuildingWorker.View>(this, BLACKSMITH_HUT_NAME);
        }
    }
}
