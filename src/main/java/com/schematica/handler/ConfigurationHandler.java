package com.schematica.handler;

public class ConfigurationHandler
{
    public static final String VERSION = "1";

    public static final boolean ENABLE_ALPHA_DEFAULT = false;
    public static final double ALPHA_DEFAULT = 0.5;
    public static final boolean HIGHLIGHT_DEFAULT = true;
    public static final boolean HIGHLIGHT_AIR_DEFAULT = true;
    public static final double BLOCK_DELTA_DEFAULT = 0.005;
    public static final int RENDER_DISTANCE_DEFAULT = 8;

    public static boolean enableAlpha = ENABLE_ALPHA_DEFAULT;
    public static float alpha = (float) ALPHA_DEFAULT;
    public static boolean highlight = HIGHLIGHT_DEFAULT;
    public static boolean highlightAir = HIGHLIGHT_AIR_DEFAULT;
    public static double blockDelta = BLOCK_DELTA_DEFAULT;
    public static int renderDistance = RENDER_DISTANCE_DEFAULT;


    private ConfigurationHandler() {}

}
