package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobPlaceholder;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nonnull;

public class BuildingBlacksmith extends AbstractBuildingWorker
{
    private static final String BLACKSMITH          = "Blacksmith";
    private static final String BLACKSMITH_HUT_NAME = "blacksmithHut";

    public BuildingBlacksmith(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Nonnull
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

    @Nonnull
    @Override
    public String getJobName()
    {
        return BLACKSMITH;
    }

    @Nonnull
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

        @Nonnull
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<AbstractBuildingWorker.View>(this, BLACKSMITH_HUT_NAME);
        }
    }
}
