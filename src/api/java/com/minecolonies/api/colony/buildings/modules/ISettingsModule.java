package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.settings.ISetting;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;

/**
 * Settings module interface.
 */
public interface ISettingsModule
{
    /**
     * Register a new setting.
     * @param key the key of the setting.
     * @param setting the setting.
     * @return the instance of the module.
     */
    IBuildingModule with(final String key, final ISetting setting);

    /**
     * Get a specific setting.
     * @param id the id of the setting.
     * @param clazz the class of the setting.
     * @param <T> the type of setting.
     * @return the setting.
     */
    <T extends ISetting> T getSetting(final ISettingKey<T> key);
}
