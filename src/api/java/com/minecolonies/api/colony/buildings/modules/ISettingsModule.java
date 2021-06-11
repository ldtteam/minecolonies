package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;

/**
 * Settings module interface.
 */
public interface ISettingsModule extends IBuildingModule
{
    /**
     * Register a new setting.
     * @param key the key of the setting.
     * @param setting the setting.
     * @return the instance of the module.
     */
    ISettingsModule with(final ISettingKey<?> key, final ISetting setting);

    /**
     * Get a specific setting.
     * @param key the key of the setting.
     * @param <T> the type of setting.
     * @return the setting.
     */
    <T extends ISetting> T getSetting(final ISettingKey<T> key);

    /**
     * Update a given settings value.
     * @param settingKey the given key.
     * @param value the value.
     */
    void updateSetting(ISettingKey<?> settingKey, ISetting value);
}
