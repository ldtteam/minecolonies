package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;

/**
 * Module containing all settings (client side).
 */
public class TownHallSettingsModuleView extends SettingsModuleView implements ISettingsModuleView
{
    /**
     * Whether this module appears as a GUI page.
     *
     * @return true to show the GUI page.
     */
    public boolean isPageVisible() {return false;}
}
