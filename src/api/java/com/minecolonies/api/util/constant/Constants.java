package com.minecolonies.api.util.constant;

import net.minecraft.potion.Potion;

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
    public static final int    MINUTES_A_DAY                    = 20;
    public static final int    TWENTYFIVESEC                    = 25;
    public static final int    STACKSIZE                        = 64;
    public static final int    MAX_BARBARIAN_HORDE_SIZE         = 40;
    public static final int    MIN_BARBARIAN_HORDE_SIZE         = 6;
    public static final int    MAX_SPAWN_BARBARIAN_HORDE_SIZE   = 10;
    public static final int    MIN_SPAWN_BARBARIAN_HORDE_SIZE   = 1;
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
    public static final int    TICKS_FOURTY_MIN                 = 48000;
    public static final int    MAX_PARAMS_IRECIPESTORAGE        = 4;
    public static final int    MIN_PARAMS_IRECIPESTORAGE        = 3;
    public static final String DEFAULT_STYLE                    = "wooden";
    public static final String SAPLINGS                         = "treeSapling";
    public static final String ORES                             = "ores";
    public static final double HALF_BLOCK                       = 0.5D;
    public static final double SLIGHTLY_UP                      = 0.1D;
    public static final int    ONE_HUNDRED_PERCENT              = 100;
    public static final int    DOUBLE                           = 2;
    public static final int    TRIPLE                           = 3;
    public static final int    BLOCKS_PER_CHUNK                 = 16;
    public static final int    NINETY_DEGREE                    = 90;
    public static final int    HALF_ROTATION                    = 180;
    public static final int    THREE_QUARTERS                   = 270;
    public static final float  BED_HEIGHT                       = 0.6875F;
    public static final float  SLEEPING_RENDER_OFFSET           = -1.5F;
    public static final double DEFAULT_VOLUME                   = 0.2;
    public static final double DEFAULT_PITCH_MULTIPLIER         = 0.7D;
    public static final double XP_PARTICLE_EXPLOSION_SIZE       = 20;
    public static final double DEFAULT_SPEED                    = 0.6D;
    public static final float  WATCH_CLOSEST                    = 6.0F;
    public static final float  WATCH_CLOSEST2                   = 3.0F;
    public static final float  WATCH_CLOSEST2_FAR               = 5.0F;
    public static final float  WATCH_CLOSEST2_FAR_CHANCE        = 0.02F;

    /**
     * The oredict entry of an ore.
     */
    public static final String ORE_STRING = "ore";

    /**
     * Max crafting cycle depth.
     */
    public static final int MAX_CRAFTING_CYCLE_DEPTH = 20;

    /**
     * Each x blocks walked an action will be triggered to decrease saturation.
     */
    public static final int ACTIONS_EACH_BLOCKS_WALKED = 25;

    /**
     * Slot with the result of the furnace.
     */
    public static final int RESULT_SLOT = 2;

    /**
     * Slot where ores should be put in the furnace.
     */
    public static final int SMELTABLE_SLOT = 0;

    /**
     * Slot where the fuel should be put in the furnace.
     */
    public static final int FUEL_SLOT = 1;

    /**
     * Maximum message size from client to server (Leaving some extra space).
     */
    public static final int MAX_MESSAGE_SIZE = 30_000;

    /**
     * Maximum amount of pieces from client to server (Leaving some extra space).
     */
    public static final int MAX_AMOUNT_OF_PIECES = 20;

    /**
     * Max schematic size to create.
     */
    public static final int MAX_SCHEMATIC_SIZE = 100_000;

    /**
     * Tag compound of forge.
     */
    public static final int TAG_COMPOUND = 10;

    /**
     * Default size of the inventory.
     */
    public static final int DEFAULT_SIZE = 27;

    /**
     * Slots per line.
     */
    public static final int SLOT_PER_LINE = 9;

    public static final String SCIMITAR_NAME     = "iron_scimitar";
    public static final String CHIEFSWORD_NAME   = "chiefsword";
    public static final Potion LEVITATION_EFFECT = Potion.getPotionById(25);
    public static final Potion GLOW_EFFECT       = Potion.getPotionById(24);

    public static final int GLOW_EFFECT_DURATION   = 20 * 30;
    public static final int GLOW_EFFECT_MULTIPLIER = 20;
    public static final int GLOW_EFFECT_DISTANCE   = 60;

    public static final int GLOW_EFFECT_DURATION_TEAM = 20 * 60 * 10;

    /**
     * The length range one patrolling operation can have on x or z.
     */
    public static final int LENGTH_RANGE = 10;

    /**
     * The length range one patrolling operation can have on y.
     */
    public static final int UP_DOWN_RANGE = 4;

    /**
     * Max tries to find a position to path to.
     */
    public static final int MAX_TRIES = 20;

    /**
     * Private constructor to hide implicit public one.
     */
    private Constants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
