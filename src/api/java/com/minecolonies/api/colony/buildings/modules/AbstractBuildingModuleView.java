package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;

/**
 * Abstract class for all modules. Has base methods for all the necessary methods that have to be called from the building.
 */
public abstract class AbstractBuildingModuleView implements IBuildingModuleView
{
    /**
     * The building this module belongs to.
     */
    protected IBuildingView buildingView;

    @Override
    public IBuildingModuleView setBuildingView(final IBuildingView buildingView)
    {
        this.buildingView = buildingView;
        return this;
    }

    @Override
    public IColonyView getColony()
    {
        return buildingView.getColony();
    }

    /**
     * Check if a worker from this module can be directly hired as a specific job.
     * @param jobEntry the job to check for.
     * @return true if so. Defaults to false.
     */
    public boolean canBeHiredAs(final JobEntry jobEntry)
    {
        return false;
    }
}
