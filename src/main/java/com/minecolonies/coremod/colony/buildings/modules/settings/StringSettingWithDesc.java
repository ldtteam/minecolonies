package com.minecolonies.coremod.colony.buildings.modules.settings;

import java.util.List;

/**
 * Stores a string-list setting (Like enum, but easily serializable).
 * TODO: Remove in future versions as this only exists right now for settings parsing purposes, this one is not necessary anymore because {@link StringSetting} contains a description by default now.
 */
public class StringSettingWithDesc extends StringSetting
{
    /**
     * Create a new string list setting.
     *
     * @param settings the overall list of settings.
     */
    public StringSettingWithDesc(final String... settings)
    {
        super(settings);
    }

    /**
     * Create a new string list setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public StringSettingWithDesc(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }
}
