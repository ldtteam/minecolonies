package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGlassblower;

/**
 * Glassblower window class. Specifies the extras the glassblower has for its list.
 */
public class WindowHutGlassblower extends AbstractWindowWorkerBuilding<BuildingGlassblower.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutglassblower.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutGlassblower(final BuildingGlassblower.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.glassblower";
    }
}
