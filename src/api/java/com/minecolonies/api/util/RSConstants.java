package com.minecolonies.api.util;

/**
 * Utility class for RS related constants.
 */
public final class RSConstants
{
    /**
     * Priorities of the different resolvers.
     */
    public static final int CONST_DEFAULT_RESOLVER_PRIORITY = 100;
    public static final int CONST_BUILDING_RESOLVER_PRIORITY = CONST_DEFAULT_RESOLVER_PRIORITY + 100;
    public static final int CONST_WAREHOUSE_RESOLVER_PRIORITY = CONST_DEFAULT_RESOLVER_PRIORITY + 50;
    public static final int CONST_PUB_CRAFTING_RESOLVER_PRIORITY = CONST_DEFAULT_RESOLVER_PRIORITY + 40;
    public static final int CONST_CRAFTING_RESOLVER_PRIORITY = CONST_DEFAULT_RESOLVER_PRIORITY + 25;
    public static final int STANDARD_PLAYER_REQUEST_PRIORITY = 0;
    public static final int CONST_RETRYING_RESOLVER_PRIORITY = CONST_DEFAULT_RESOLVER_PRIORITY - 50;

    /**
     * Private constructor to hide the public one.
     */
    private RSConstants()
    {
        //Hides implicit constructor.
    }
}
