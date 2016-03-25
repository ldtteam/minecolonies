package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.lib.Constants;

public class WindowHutBuilder extends WindowWorkerBuilding<BuildingBuilder.View>
{
    private static final String HUT_BUILDER_RESOURCE_SUFFIX = ":gui/windowHutBuilder.xml";

    public WindowHutBuilder(BuildingBuilder.View building)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.buildersHut";
    }
}
