package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobPlaceholder;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingStonemason extends AbstractBuildingWorker
{

    private static final String STONEMASON          = "Stonemason";
    private static final String STONEMASON_HUT_NAME = "stonemasonHut";

    public BuildingStonemason(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return STONEMASON;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return STONEMASON;
    }

    @NotNull
    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobPlaceholder(citizen); //TODO Implement Later
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, STONEMASON_HUT_NAME);
        }
    }
}
