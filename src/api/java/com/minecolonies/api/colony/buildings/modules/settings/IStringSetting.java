package com.minecolonies.api.colony.buildings.modules.settings;

import java.util.List;

/**
 * String Setting.
 */
public interface IStringSetting extends ISetting
{
    /**
     * Get the setting value.
     * @return the current value.
     */
    String getValue();

    /**
     * Get the default value.
     * @return the default value.
     */
    String getDefault();

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
    void set(final String value);

    @Override
    default void copyValue(final ISetting iSetting)
    {
        if (iSetting instanceof final IStringSetting other)
        {
            set(other.getValue());
        }
    }
}
