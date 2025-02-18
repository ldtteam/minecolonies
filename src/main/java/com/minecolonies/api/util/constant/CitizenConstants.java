package com.minecolonies.api.util.constant;

/**
 * Constants regarding citizens.
 */
public final class CitizenConstants
{
    /**
     * The default limit of citizens, if no research is done yet.
     */
    public static final int CITIZEN_LIMIT_DEFAULT = 25;

    /**
     * The limit of citizens if the first increase research is done.
     */
    public static final int CITIZEN_LIMIT_OUTPOST = 50;

    /**
     * The limit of citizens if the second increase research is done.
     */
    public static final int CITIZEN_LIMIT_HAMLET = 100;

    /**
     * The limit of citizens if the third increase research is done.
     */
    public static final int CITIZEN_LIMIT_VILLAGE = 150;

    /**
     * The limit of citizens if the last increase research is done, this is absolute limit of citizens, including config file.
     */
    public static final int CITIZEN_LIMIT_MAX = 500;

    /**
     * Base movement speed of every citizen.
     */
    public static final double BASE_MOVEMENT_SPEED = 0.3D;

    /**
     * The middle saturation point. smaller than this = bad and bigger than this = good.
     */
    public static final int AVERAGE_SATURATION = 10;

    /**
     * Lower than this is low saturation.
     */
    public static final int LOW_SATURATION = 6;

    /**
     * Full saturation amount.
     */
    public static final double FULL_SATURATION = 20;

    /**
     * Number of ticks to heal the citizens.
     */
    public static final int HEAL_CITIZENS_AFTER = 100;

    /**
     * The delta yaw value for looking at things.
     */
    public static final float  FACING_DELTA_YAW           = 10F;
    /**
     * The range in which we can hear a block break sound.
     */
    public static final double BLOCK_BREAK_SOUND_RANGE    = 16.0D;
    /**
     * The range in which someone will see the particles from a block breaking.
     */
    public static final double BLOCK_BREAK_PARTICLE_RANGE = 16.0D;
    /**
     * Quantity to be moved to rotate without actually moving.
     */
    public static final double MOVE_MINIMAL               = 0.001D;
    /**
     * Base max health of the citizen.
     */
    public static final double BASE_MAX_HEALTH            = 20D;
    /**
     * Base pathfinding range of the citizen.
     */
    public static final int    BASE_PATHFINDING_RANGE     = 100;
    /**
     * Height of the citizen.
     */
    public static final double CITIZEN_HEIGHT             = 1.8D;
    /**
     * Width of the citizen.
     */
    public static final double CITIZEN_WIDTH              = 0.6D;
    /**
     * The speed the citizen has to rotate.
     */
    public static final double ROTATION_MOVEMENT          = 30;
    /**
     * 20 ticks or also: once a second.
     */
    public static final int    TICKS_20                   = 20;

    /**
     * This times the citizen id is the personal offset of the citizen.
     */
    public static final int    OFFSET_TICK_MULTIPLIER     = 7;
    /**
     * Range required for the citizen to be home.
     */
    public static final double RANGE_TO_BE_HOME           = 16;
    /**
     * The max amount of lines the latest log allows.
     */
    public static final int    MAX_LINES_OF_LATEST_LOG    = 4;
    /**
     * Distance from mobs the entity should hold.
     */
    public static final double DISTANCE_OF_ENTITY_AVOID   = 5.0D;
    /**
     * Initital speed while running away from entities.
     */
    public static final double INITIAL_RUN_SPEED_AVOID    = 1.1D;
    /**
     * Later run speed while running away from entities.
     */
    public static final double LATER_RUN_SPEED_AVOID      = 0.8D;
    /**
     * The max squaredistance a citizen can call a guard to help, 300 blocks
     */
    public static final int    MAX_GUARD_CALL_RANGE       = 90000;
    /**
     * Big multiplier in extreme saturation situations.
     */
    public static final double BIG_SATURATION_FACTOR      = 0.05;
    /**
     * Decrease by this * buildingLevel each new night.
     */
    public static final double SATURATION_DECREASE_FACTOR = 0.02;

    /**
     * The maximum range to keep from the current building place.
     */
    public static final int EXCEPTION_TIMEOUT = 100;

    /**
     * The maximum range to keep from the current building place.
     */
    public static final int MAX_ADDITIONAL_RANGE_TO_BUILD = 25;

    /**
     * Time in ticks to wait until the next check for items.
     */
    public static final int DELAY_RECHECK = 10;

    /**
     * The default range for any walking to blocks.
     */
    public static final int DEFAULT_RANGE_FOR_DELAY = 4;

    /**
     * The number of actions done before item dump.
     */
    public static final int ACTIONS_UNTIL_DUMP = 32;

    /**
     * Hit a block every x ticks when mining.
     */
    public static final int HIT_EVERY_X_TICKS = 5;

    /**
     * Min slots the builder should never fill.
     */
    public static final long MIN_OPEN_SLOTS = 5;

    /**
     * The max citizen level.
     */
    public static final int MAX_CITIZEN_LEVEL = 99;

    /**
     * The Guard Building health modifier Name
     */
    public static final String GUARD_HEALTH_MOD_BUILDING_NAME = "MinecoloniesGuardBuildingHP";

    /**
     * The Research health modifier name.
     */
    public static final String RESEARCH_BONUS_MULTIPLIER = "ResearchSpeedBonus";

    /**
     * The addition skill bonus speed modifier
     */
    public static final String SKILL_BONUS_ADD = "SkillSpeedBonus";

    /**
     * The Config guard health modifier name
     */
    public static final String GUARD_HEALTH_MOD_CONFIG_NAME = "MinecoloniesGuardConfigHP";

    /**
     * The guard's level based health bonus mod's name
     */
    public static final String GUARD_HEALTH_MOD_LEVEL_NAME = "MinecoloniesGuardLevelHealth";

    /**
     * At this stack size or smaller the chance to dump is 50%.
     */
    public static final int CHANCE_TO_DUMP_50 = 16;

    /**
     * Chance to dump, if lower then this then dump else not.
     */
    public static final int CHANCE_TO_DUMP = 8;

    /**
     * Disease tag,
     */
    public static final String TAG_DISEASE = "disease";

    /**
     * Disease iod tag.
     */
    public static final String TAG_DISEASE_ID = "disease_id";

    /**
     * Disease immunity tag,
     */
    public static final String TAG_IMMUNITY = "immunity";

    /**
     * Noon day time.
     */
    public static final int NOON = 6000;

    /**
     * Nighttime, point at which you can sleep.
     */
    public static final int NIGHT = 12600;

    /**
     * The minimum range to keep from the current building place.
     */
    public static final int MIN_ADDITIONAL_RANGE_TO_BUILD = 3;

    /**
     * String which shows if something is a waypoint.
     */
    public static final String WAYPOINT_STRING = "infrastructure";

    /**
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally).
     */
    public static final double XP_EACH_BUILDING = 8.0D;

    /**
     * Increase this value to make the building speed slower. Used to balance worker level speed increase.
     */
    public static final int PROGRESS_MULTIPLIER = 10;

    /**
     * Speed the builder should run away when he castles himself in.
     */
    public static final double RUN_AWAY_SPEED = 4.1D;

    /**
     * The standard range the builder should reach until his target.
     */
    public static final int STANDARD_WORKING_RANGE = 5;

    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    public static final int MIN_WORKING_RANGE = 12;

    /**
     * Disabled timer.
     */
    public static final int DISABLED = -1;
}
