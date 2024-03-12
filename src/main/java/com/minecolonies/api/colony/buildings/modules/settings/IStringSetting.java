package com.minecolonies.api.colony.buildings.modules.settings;

import java.util.List;

/**
 * String Setting.
 */
public interface IStringSetting<S> extends ISetting<S>
{
    /**
     * Get the setting value.
     * @return the current value.
     */
    S getValue();

    /**
     * Get the default value.
     * @return the default value.
     */
    S getDefault();

    /**
     * Get the current index of the setting.
     * @return the index.
     */
    int getCurrentIndex();

    /**
     * Get the list of all settings.
     * @return a copy of the list.
     */
    List<String> getSettings();

    /**
     * Set the setting to a specific index.
     * @param value the value to set.
     */
    void set(final S value);
}
