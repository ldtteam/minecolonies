package com.minecolonies.coremod.client.gui;

import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.lib.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the home building.
 */
public class WindowHomeBuilding extends AbstractWindowBuilding<BuildingHome.View>
{
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhuthome.xml";

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowHomeBuilding(final BuildingHome.View building)
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
        return "com.minecolonies.coremod.gui.workerHuts.homeHut";
    }
}
