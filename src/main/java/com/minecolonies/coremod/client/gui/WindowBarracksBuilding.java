package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.BuildingBarracks;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the barracks building.
 */
public class WindowBarracksBuilding extends AbstractWindowBuilding<BuildingBarracks.View>
{
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowHutBarracks.xml";

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowBarracksBuilding(final BuildingBarracks.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.buildBarracks";
    }
}
