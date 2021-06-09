package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;

import java.util.List;

/**
 * Stores the follow mode setting.
 */
public class FollowModeSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static String TIGHT =  "com.minecolonies.core.guard.setting.follow.tight";
    public static String LOOSE =  "com.minecolonies.core.guard.setting.follow.loose";

    /**
     * Create a new follow mode setting.
     */
    public FollowModeSetting()
    {
        super(TIGHT, LOOSE);
    }

    /**
     * Create a new follow mode setting.
     * @param settings the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public FollowModeSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.FOLLOW);
    }
}
