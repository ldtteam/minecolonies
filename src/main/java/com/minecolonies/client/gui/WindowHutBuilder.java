package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.lib.Constants;

public class WindowHutBuilder extends WindowWorkerBuilding<BuildingBuilder.View>
{
    public WindowHutBuilder(BuildingBuilder.View building)
    {
        super(building, Constants.MODID + ":gui/windowHutBuilder.xml");
    }
}
