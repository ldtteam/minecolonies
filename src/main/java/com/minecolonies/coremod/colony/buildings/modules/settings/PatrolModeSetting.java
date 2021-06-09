package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;

import java.util.List;

/**
 * Stores the patrol mode setting.
 */
public class PatrolModeSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static String AUTO =  "com.minecolonies.core.guard.setting.patrol.auto";
    public static String MANUAL =  "com.minecolonies.core.guard.setting.patrol.manual";

    /**
     * Create a new patrol mode list setting.
     */
    public PatrolModeSetting()
    {
        super(AUTO, MANUAL);
    }

    /**
     * Create a new patrol mode list setting.
     * @param settings the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public PatrolModeSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.PATROL);
    }
}
