package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDyer;

/**
 * Dyer window class. Specifies the extras the dyer has for its list.
 */
public class WindowHutDyer extends AbstractWindowWorkerBuilding<BuildingDyer.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutdyer.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutDyer(final BuildingDyer.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.dyer";
    }
}
