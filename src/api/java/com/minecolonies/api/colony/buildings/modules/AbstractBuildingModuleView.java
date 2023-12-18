package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.Log;

/**
 * Abstract class for all modules. Has base methods for all the necessary methods that have to be called from the building.
 */
public abstract class AbstractBuildingModuleView implements IBuildingModuleView
{
    /**
     * The building this module belongs to.
     */
    protected IBuildingView buildingView;

    /**
     * The creator and identity of this module
     */
    private BuildingEntry.ModuleProducer producer = null;

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

    @Override
    public IBuildingView getBuildingView()
    {
        return buildingView;
    }

    @Override
    public <M extends IBuildingModule, V extends IBuildingModuleView> IBuildingModuleView setProducer(final BuildingEntry.ModuleProducer<M,V> moduleSet)
    {
        if (producer != null)
        {
            Log.getLogger().error("Changing a producer is not allowed, trace:", new Exception());
            return this;
        }
        this.producer = moduleSet;
        return this;
    }

    @Override
    public <M extends IBuildingModule, V extends IBuildingModuleView> BuildingEntry.ModuleProducer<M,V> getProducer()
    {
        return producer;
    }
}
