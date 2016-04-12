package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.lib.Constants;

/**
 * Window for worker.
 * Placeholder for many different jobs
 * @param <B>        Object extending {@link com.minecolonies.colony.buildings.BuildingWorker.View}
 */
public class WindowHutWorkerPlaceholder<B extends BuildingWorker.View> extends AbstractWindowWorkerBuilding<B>
{
    private                 String name;
    private static final    String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/windowHutWorkerPlaceholder.xml";

    /**
     * Window for worker placeholder.
     * Used by Baker, Blacksmith, Lumberjack ans Stonemason
     *
     * @param building      Building extending {@link com.minecolonies.colony.buildings.BuildingWorker.View}
     * @param name          Name of the the view (resource)
     */
    public WindowHutWorkerPlaceholder(B building, String name)
    {
        super(building, Constants.MOD_ID + WORKER_PLACEHOLDER_RESOURCE_SUFFIX);
        this.name = name;
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts." + name;
    }
}
