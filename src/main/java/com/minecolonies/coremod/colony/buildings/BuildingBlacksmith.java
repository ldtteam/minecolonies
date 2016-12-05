package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobPlaceholder;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingBlacksmith extends AbstractBuildingWorker
{
    private static final String BLACKSMITH          = "Blacksmith";
    private static final String BLACKSMITH_HUT_NAME = "blacksmithHut";

    public BuildingBlacksmith(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
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

    @NotNull
    @Override
    public String getJobName()
    {
        return BLACKSMITH;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobPlaceholder(citizen); //TODO Implement Later
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<AbstractBuildingWorker.View>(this, BLACKSMITH_HUT_NAME);
        }
    }
}
