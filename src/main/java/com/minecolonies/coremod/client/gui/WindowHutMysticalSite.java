package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;

/**
 * Window for the home building.
 */
public class WindowHutMysticalSite extends AbstractWindowBuilding<BuildingMysticalSite.View>
{
    /**
     * Suffix describing the window xml.
     */
    private static final String MYSTICAL_SITE_BUILDING_RESOURCE_SUFFIX = ":gui/windowhutmysticalsite.xml";

    /**
     * The building the view is relates to.
     */
    private final BuildingMysticalSite.View mysticalSite;

    /**
     * The name of the specific one.
     */
    private final String name;

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowHutMysticalSite(final BuildingMysticalSite.View building, final String name)
    {
        super(building, Constants.MOD_ID + MYSTICAL_SITE_BUILDING_RESOURCE_SUFFIX);
        this.mysticalSite = building;
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
