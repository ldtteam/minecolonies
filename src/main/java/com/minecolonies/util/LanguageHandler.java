package com.minecolonies.util;

import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * Created by Colton on 5/12/2014.
 */
public class LanguageHandler
{
    private static String getString(String key)
    {
        return LanguageRegistry.instance().getStringLocalization(key);
    }

    public static String format(String key, Object... args)
    {
        return String.format(getString(key), args);
    }
}
