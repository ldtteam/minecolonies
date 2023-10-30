package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;

import java.util.List;

import static com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting.PATROL;

/**
 * Stores the patrol mode setting.
 */
public class GuardPatrolModeSetting extends StringSettingWithDesc
{
    /**
     * Different setting possibilities.
     */
    public static final String AUTO   = "com.minecolonies.core.guard.setting.patrol.auto";
    public static final String MANUAL = "com.minecolonies.core.guard.setting.patrol.manual";

    /**
     * Create a new patrol mode list setting.
     */
    public GuardPatrolModeSetting()
    {
        super(AUTO, MANUAL);
    }

    /**
     * Create a new patrol mode list setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public GuardPatrolModeSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public boolean isActive(final ISettingsModule module)
    {
        return module.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(PATROL);
    }

    @Override
    public boolean isActive(final ISettingsModuleView module)
    {
        return module.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(PATROL);
    }

    @Override
    public boolean shouldHideWhenInactive()
    {
        return true;
    }
}
