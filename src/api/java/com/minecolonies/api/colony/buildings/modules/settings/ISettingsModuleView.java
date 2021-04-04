package com.minecolonies.api.colony.buildings.modules.settings;

/**
 * Client side part of the settings module.
 */
public interface ISettingsModuleView
{
    /**
     * Trigger a setting of a specific key.
     * @param key the settings key.
     */
    void trigger(final ISettingKey<?> key);
}
