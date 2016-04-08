package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.lib.Constants;

public class WindowHutWorkerPlaceholder<BUILDING extends BuildingWorker.View> extends WindowWorkerBuilding<BUILDING>
{
    private                 String name;
    private static final    String WORKER_PLACEHOLDER_RESOURCE_SUFFIX = ":gui/windowHutWorkerPlaceholder.xml";

    public WindowHutWorkerPlaceholder(BUILDING building, String name)
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
