package com.schematica.handler;

/**
 * Stores static config for schematic rendering.
 */
public final class ConfigurationHandler
{
    public static final boolean enableAlpha    = false;
    public static final float   alpha          = 0.5F;
    public static final boolean highlight      = true;
    public static final boolean highlightAir   = true;
    public static final double  blockDelta     = 0.005;
    public static final int     renderDistance = 8;

    private ConfigurationHandler()
    {
    }
}
