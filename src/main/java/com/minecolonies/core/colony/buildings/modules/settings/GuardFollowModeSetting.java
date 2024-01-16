package com.minecolonies.core.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;

import java.util.List;

/**
 * Stores the follow mode setting.
 */
public class GuardFollowModeSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static final String TIGHT = "com.minecolonies.core.guard.setting.follow.tight";
    public static final String LOOSE = "com.minecolonies.core.guard.setting.follow.loose";

    /**
     * Create a new follow mode setting.
     */
    public GuardFollowModeSetting()
    {
        super(TIGHT, LOOSE);
    }

    /**
     * Create a new follow mode setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public GuardFollowModeSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        return module.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.FOLLOW);
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.FOLLOW);
    }

    @Override
    public boolean shouldHideWhenInactive()
    {
        return true;
    }
}
