package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.jobs.registry.JobEntry;

/**
 *  Student module view.
 */
public class StudentBuildingModuleView extends WorkerBuildingModuleView
{
    @Override
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return true;
    }
}
