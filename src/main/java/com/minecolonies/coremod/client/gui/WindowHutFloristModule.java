package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Florist window class. Specifies the extras the florist has for its list.
 */
public class WindowHutFloristModule extends AbstractWindowWorkerModuleBuilding<BuildingFlorist.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutflorist.xml";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutFloristModule(final BuildingFlorist.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
    }

    @Override
    public String getBuildingName()
    {
        return FLORIST_BUILDING_NAME;
    }
}
