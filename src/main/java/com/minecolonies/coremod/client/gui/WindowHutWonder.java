package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.BuildingWonder;
import org.jetbrains.annotations.NotNull;

/**
 * Window for the home building.
 */
public class WindowHutWonder extends AbstractWindowBuilding<BuildingWonder.View>
{
    /**
     * Suffix describing the window xml.
     */
    private static final String WONDER_BUILDING_RESOURCE_SUFFIX = ":gui/windowhutwonder.xml";

    /**
     * The building the view is relates to.
     */
    private final BuildingWonder.View wonder;

    /**
     * The name of the specific one.
     */
    private final String name;

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowHutWonder(final BuildingWonder.View building, final String name)
    {
        super(building, Constants.MOD_ID + WONDER_BUILDING_RESOURCE_SUFFIX);
        this.wonder = building;
        this.name = name;
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts." + name;
    }
}
