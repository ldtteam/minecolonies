package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;

/**
 *  Student module view.
 */
public class KnightSquireBuildingModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return jobEntry == ModJobs.knight.get();
    }
}
