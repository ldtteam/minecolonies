package com.minecolonies.api.util.constant;

/**
 * Some constants needed for the whole mod.
 */
public final class Constants
{
    public static final String MOD_ID                           = "minecolonies";
    public static final String MOD_NAME                         = "MineColonies";
    public static final String VERSION                          = "@VERSION@";
    public static final String FORGE_VERSION                    = "required-after:Forge@[14.22.0.2459,)";
    public static final String MC_VERSION                       = "[1.12,1.13]";
    public static final String CLIENT_PROXY_LOCATION            = "com.minecolonies.coremod.proxy.ClientProxy";
    public static final String SERVER_PROXY_LOCATION            = "com.minecolonies.coremod.proxy.ServerProxy";
    public static final String CONFIG_GUI_LOCATION              = "com.minecolonies.coremod.client.gui.GuiFactory";
    public static final String PLAYER_PROPERTY_NAME             = MOD_ID + ".PlayerProperties";
    public static final String HARVESTCRAFTMODID                = "harvestcraft";
    public static final int    YELLOW                           = 4;
    public static final int    ROTATE_0_TIMES                   = 0;
    public static final int    ROTATE_ONCE                      = 1;
    public static final int    ROTATE_TWICE                     = 2;
    public static final int    ROTATE_THREE_TIMES               = 3;
    public static final int    CITIZEN_RESPAWN_INTERVAL_MIN     = 10;
    public static final int    CITIZEN_RESPAWN_INTERVAL_MAX     = 600;
    public static final int    MAX_BUILDING_LEVEL               = 5;
    public static final int    TICKS_SECOND                     = 20;
    public static final int    SECONDS_A_MINUTE                 = 60;
    public static final int    STACKSIZE                        = 64;
    public static final int    MAX_BARBARIAN_HORDE_SIZE         = 60;
    public static final int    MIN_BARBARIAN_HORDE_SIZE         = 5;
    public static final int    MAX_BARBARIAN_DIFFICULTY         = 10;
    public static final int    MIN_BARBARIAN_DIFFICULTY         = 0;
    public static final int    ENTITY_TRACKING_RANGE            = 256;
    public static final int    ENTITY_UPDATE_FREQUENCY          = 2;
    public static final int    ENTITY_UPDATE_FREQUENCY_FISHHOOK = 5;
    public static final int    MAX_ROTATIONS                    = 4;
    public static final double WHOLE_CIRCLE                     = 360.0;
    public static final double HALF_A_CIRCLE                    = 180;
    public static final int    UPDATE_FLAG                      = 0x03;
    public static final int    TICKS_HOUR                       = TICKS_SECOND * SECONDS_A_MINUTE * SECONDS_A_MINUTE;
    public static final int MAX_PARAMS_IRECIPESTORAGE = 4;
    public static final int MIN_PARAMS_IRECIPESTORAGE = 3;
    public static final String DEFAULT_STYLE                    = "wooden";
    public static final String SAPLINGS                         = "treeSapling";
    public static final String ORES                             = "ores";
    public static final double HALF_BLOCK                       = 0.5D;
    public static final double SLIGHTLY_UP                      = 0.1D;
    public static final int    ONE_HUNDRED_PERCENT              = 100;
    public static final int    DOUBLE                           = 2;
    public static final int    TRIPLE                           = 3;

    /**
     * Private constructor to hide implicit public one.
     */
    private Constants()
    {
        /**
         * Intentionally left empty.
          */
    }
}
