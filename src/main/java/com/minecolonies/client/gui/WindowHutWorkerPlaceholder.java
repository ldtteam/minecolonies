package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.lib.Constants;

public class WindowHutWorkerPlaceholder<BUILDING extends BuildingWorker.View> extends WindowWorkerBuilding<BUILDING>
{
    private String name;

    public WindowHutWorkerPlaceholder(BUILDING building, String name)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutWorkerPlaceholder.xml");
        this.name = name;
    }

    public String getBuildingName() { return "com.minecolonies.gui.workerHuts." + name; }
}
