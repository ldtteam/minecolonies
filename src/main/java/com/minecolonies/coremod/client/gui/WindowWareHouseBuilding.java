package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.BuildingWareHouse;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the home building.
 */
public class WindowWareHouseBuilding extends AbstractWindowBuilding<BuildingWareHouse.View>
{
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowHutWarehouse.xml";

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowWareHouseBuilding(final BuildingWareHouse.View building)
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
        return "com.minecolonies.coremod.gui.workerHuts.buildingWareHouse";
    }
}
