package com.minecolonies.configuration;

public class Configurations
{
    /*
    Defaults
     */
    public static final int     DEFAULT_WORKINGRANGETOWNHALL     = 100;
    public static final int     DEFAULT_TOWNHALLPADDING          = 20;
    public static final boolean DEFAULT_ALLOWINFINTESUPPLYCHESTS = false;
    public static final int     DEFAULT_CITIZENRESPAWNINTERVAL   = 30;
    public static final boolean DEFAULT_BUILDERINFINITERESOURCES = true;//TODO change to false when material handling is implemented

    /*
    Holders
     */
    public static int     workingRangeTownhall;
    public static int     townhallPadding;
    public static boolean allowInfiniteSupplyChests;
    /**
     * The citizen respawn interval in seconds
     */
    public static int     citizenRespawnInterval;
    public static boolean builderInfiniteResources;
}
