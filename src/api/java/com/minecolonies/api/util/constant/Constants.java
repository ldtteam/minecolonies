package com.minecolonies.api.util.constant;

/**
 * Some constants needed for the whole mod.
 */
public class Constants
{
    public static final String MOD_ID                           = "minecolonies";
    public static final String MOD_NAME                         = "MineColonies";
    public static final String VERSION                          = "@VERSION@";
    public static final String FINGERPRINT                      = "@FINGERPRINT@";
    public static final String FORGE_VERSION                    = "required-after:Forge@[12.18.1.2076,)";
    public static final String MC_VERSION                       = "[1.10.2]";
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
    public static final int    MAX_BUILDING_LEVEL               = 5;
    public static final int    TICKS_SECOND                     = 20;
    public static final int    SECONDS_A_MINUTE                 = 60;
    public static final int    STACKSIZE                        = 64;
    public static final int    MAX_BARBARIAN_HORDE_SIZE         = 60;
    public static final int    MIN_BARBARIAN_HORDE_SIZE         = 5;
    public static final int    ENTITY_TRACKING_RANGE            = 256;
    public static final int    ENTITY_UPDATE_FREQUENCY          = 2;
    public static final int    ENTITY_UPDATE_FREQUENCY_FISHHOOK = 5;
    public static final int    MAX_ROTATIONS                    = 4;
    public static final double WHOLE_CIRCLE                     = 360.0;
    public static final double HALF_A_CIRCLE                    = 180;
    public static final int    UPDATE_FLAG                      = 0x03;
}
