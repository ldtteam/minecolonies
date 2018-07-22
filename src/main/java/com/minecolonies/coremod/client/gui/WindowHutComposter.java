package com.minecolonies.coremod.client.gui;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;

public class WindowHutComposter extends AbstractWindowWorkerBuilding<BuildingComposter.View>
{
    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending {@link AbstractBuildingWorker.View}.
     * @param resource Resource of the window.
     */
    public WindowHutComposter(final BuildingComposter.View building, final String resource)
    {
        super(building, resource);
    }

    @Override
    public String getBuildingName()
    {
        return "Composter Hut";
    }
}
