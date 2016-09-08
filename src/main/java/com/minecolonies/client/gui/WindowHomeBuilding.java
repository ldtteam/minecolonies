package com.minecolonies.client.gui;

import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.lib.Constants;
import javax.annotation.Nonnull;

/**
 * Window for the home building
 */
public class WindowHomeBuilding extends AbstractWindowBuilding<BuildingHome.View>
{
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowHutHome.xml";

    /**
     * Creates the Window object
     *
     * @param building View of the home building
     */
    public WindowHomeBuilding(BuildingHome.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);
    }

    /**
     * Returns the name of a building
     *
     * @return Name of a building
     */
    @Nonnull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.homeHut";
    }
}
