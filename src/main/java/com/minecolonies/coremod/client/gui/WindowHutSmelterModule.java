package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;

/**
 * Smelter window class. Specifies the extras the smelter has for its list.
 */
public class WindowHutSmelterModule extends AbstractWindowWorkerModuleBuilding<BuildingSmeltery.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutsmelter.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutSmelterModule(final BuildingSmeltery.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.smelter";
    }
}
