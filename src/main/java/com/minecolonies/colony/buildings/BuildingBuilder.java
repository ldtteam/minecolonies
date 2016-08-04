package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutBuilder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobBuilder;
import net.minecraft.util.BlockPos;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingWorker
{
    private static final String BUILDER     = "Builder";

    public BuildingBuilder(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return BUILDER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 2;
    }

    @Override
    public String getJobName()
    {
        return BUILDER;
    }

    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobBuilder(citizen);
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutBuilder(this);
        }
    }
}
