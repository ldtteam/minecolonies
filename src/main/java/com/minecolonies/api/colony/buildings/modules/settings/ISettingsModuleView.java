package com.minecolonies.api.colony.buildings.modules.settings;

import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import org.jetbrains.annotations.Nullable;

/**
 * Client side part of the settings module.
 */
public interface ISettingsModuleView extends IBuildingModuleView
{
    /**
     * Trigger a setting of a specific key.
     * @param key the settings key.
     */
    void trigger(final ISettingKey<?> key);

    /**
     * Get a specific setting.
     * @param key the key of the setting.
     * @param <T> the type of setting.
     * @return the setting.
     */
    @Nullable
    <T extends ISetting> T getSetting(final ISettingKey<T> key);
}
