package com.minecolonies.api.lib;

import com.minecolonies.api.reference.ModAchievements;

/**
 * Some constants needed for the whole mod.
 */
public class Constants
{
    public static final String MOD_ID                = "minecolonies";
    public static final String MOD_NAME              = "MineColonies";
    public static final String VERSION               = "@VERSION@";
    public static final String FORGE_VERSION         = "required-after:Forge@[12.18.1.2076,)";
    public static final String MC_VERSION            = "[1.11]";
    public static final String CLIENT_PROXY_LOCATION = "com.minecolonies.coremod.proxy.ClientProxy";
    public static final String SERVER_PROXY_LOCATION = "com.minecolonies.coremod.proxy.ServerProxy";
    public static final String CONFIG_GUI_LOCATION   = "com.minecolonies.coremod.client.gui.GuiFactory";
    public static final String PLAYER_PROPERTY_NAME  = MOD_ID + ".PlayerProperties";
    public static final int    YELLOW                = 4;
    public static final int    ROTATE_0_TIMES        = 0;
    public static final int    ROTATE_ONCE           = 1;
    public static final int    ROTATE_TWICE          = 2;
    public static final int    ROTATE_THREE_TIMES    = 3;
    /**
     * Population size to achieve {@link ModAchievements#achievementSizeSettlement}.
     */
    public static int ACHIEVEMENT_SIZE_SETTLEMENT = 5;
    /**
     * Population size to achieve {@link ModAchievements#achievementSizeTown}.
     */
    public static int ACHIEVEMENT_SIZE_TOWN = 10;
    /**
     * Population size to achieve {@link ModAchievements#achievementSizeCity}.
     */
    public static int ACHIEVEMENT_SIZE_CITY = 20;
    /**
     * Population size to achieve {@link ModAchievements#achievementSizeMetropolis}.
     */
    public static int ACHIEVEMENT_SIZE_METROPOLIS = 50;
}
