package com.minecolonies.lib;

import com.minecolonies.colony.permissions.Permissions;

import java.util.HashMap;
import java.util.Map;

public class Constants
{
    public static final String MOD_ID                = "minecolonies";
    public static final String MOD_NAME              = "MineColonies";
    public static final String VERSION               = "@VERSION@";
    public static final String FINGERPRINT           = "@FINGERPRINT@";
    public static final String CLIENT_PROXY_LOCATION = "com.minecolonies.proxy.ClientProxy";
    public static final String SERVER_PROXY_LOCATION = "com.minecolonies.proxy.ServerProxy";
    public static final String PLAYER_PROPERTY_NAME  = MOD_ID + ".PlayerProperties";

    public class BlockData
    {
        public static final int TORCH_EAST  = 1;
        public static final int TORCH_WEST  = 2;
        public static final int TORCH_SOUTH = 3;
        public static final int TORCH_NORTH = 4;

        public static final int BUTTON_LEVER_CEILING = 0;
        public static final int BUTTON_LEVER_EAST    = 1;
        public static final int BUTTON_LEVER_WEST    = 2;
        public static final int BUTTON_LEVER_SOUTH   = 3;
        public static final int BUTTON_LEVER_NORTH   = 4;

        public static final int LADDER_EAST  = 5;
        public static final int LADDER_WEST  = 4;
        public static final int LADDER_SOUTH = 3;
        public static final int LADDER_NORTH = 2;

        public static final int SIGN_EAST  = 5;
        public static final int SIGN_WEST  = 4;
        public static final int SIGN_SOUTH = 3;
        public static final int SIGN_NORTH = 2;

        public static final int TRAPDOOR_EAST  = 3;
        public static final int TRAPDOOR_WEST  = 2;
        public static final int TRAPDOOR_SOUTH = 1;
        public static final int TRAPDOOR_NORTH = 0;

        public static final int VINE_EAST  = 0x8;
        public static final int VINE_WEST  = 0x2;
        public static final int VINE_SOUTH = 0x1;
        public static final int VINE_NORTH = 0x4;

        public static final int TRIPWIRE_HOOK_EAST  = 3;
        public static final int TRIPWIRE_HOOK_WEST  = 1;
        public static final int TRIPWIRE_HOOK_SOUTH = 0;
        public static final int TRIPWIRE_HOOK_NORTH = 2;

        public static final int SKULL_EAST  = 4;
        public static final int SKULL_WEST  = 5;
        public static final int SKULL_SOUTH = 3;
        public static final int SKULL_NORTH = 2;

        public static final int BUTTON_LEVER_MASK  = 0x7;
        public static final int TRAPDOOR_MASK      = 0x3;
        public static final int TRIPWIRE_HOOK_MASK = 0x3;
    }
}
