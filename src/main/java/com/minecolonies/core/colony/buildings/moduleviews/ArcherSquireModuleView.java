package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;

/**
 *  Archery module view.
 */
public class ArcherSquireModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return jobEntry == ModJobs.archer.get();
    }
}
