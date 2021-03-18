package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;

/**
 * Cook window class. Specifies the extras the composter has for its list.
 */
public class WindowHutCookModule extends AbstractWindowWorkerModuleBuilding<BuildingCook.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutcook.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutCookModule(final BuildingCook.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.cook";
    }
}
