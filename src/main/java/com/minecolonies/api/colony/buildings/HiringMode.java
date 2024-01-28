package com.minecolonies.api.colony.buildings;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Different hiring mode of buildings.
 */
public enum HiringMode
{
    DEFAULT(HIRING_MODE_DEFAULT),
    AUTO(HIRING_MODE_AUTOMATIC),
    MANUAL(HIRING_MODE_MANUAL),
    LOCKED(HIRING_MODE_LOCKED);

    private final String translationKey;

    HiringMode(String translationKey)
    {
        this.translationKey = translationKey;
    }

    public String getTranslationKey()
    {
        return translationKey;
    }
}
