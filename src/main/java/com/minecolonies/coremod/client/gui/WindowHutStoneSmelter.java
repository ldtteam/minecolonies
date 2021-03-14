package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingStoneSmeltery;

/**
 * Stone smelter window class. Specifies the extras the stone smelter has for its list.
 */
public class WindowHutStoneSmelter extends AbstractWindowWorkerBuilding<BuildingStoneSmeltery.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutstonesmelter.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutStoneSmelter(final BuildingStoneSmeltery.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.stonesmelter";
    }
}
