package com.minecolonies.api.util.constant;

/**
 * Constants regarding citizens.
 */
public final class CitizenConstants
{
    /**
     * Base movement speed of every citizen.
     */
    public static final double BASE_MOVEMENT_SPEED = 0.3D;

    /**
     * The middle saturation point. smaller than this = bad and bigger than this = good.
     */
    public static final int AVERAGE_SATURATION = 5;

    /**
     * Lower than this is low saturation.
     */
    public static final int LOW_SATURATION = 3;

    /**
     * Higher than this is high saturation.
     */
    public static final int HIGH_SATURATION = 7;

    /**
     * Full saturation amount.
     */
    public static final double FULL_SATURATION = 10;

    /**
     * The movement speed for the citizen to run away.
     */
    public static final int MOVE_AWAY_SPEED = 2;

    /**
     * The range for the citizen to move away.
     */
    public static final int MIN_MOVE_AWAY_RANGE = 3;

    /**
     * Number of retries after stuck to move away.
     */
    public static final int MOVE_AWAY_RETRIES = 3;

    /**
     * The range for the citizen to move away.
     */
    public static final int MOVE_AWAY_RANGE     = 6;

    /**
     * Number of ticks to heal the citizens.
     */
    public static final int HEAL_CITIZENS_AFTER = 100;

    /**
     * Distance to avoid Barbarian.
     */
    public static final double AVOID_BARBARIAN_RANGE      = 20D;
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
     * Divide experience by a factor to ensure more levels fit in an int.
     */
    public static final double EXP_DIVIDER                = 100.0;
    /**
     * Chance the citizen will rant about bad weather. 20 ticks per 60 seconds =
     * 5 minutes.
     */
    public static final int    RANT_ABOUT_WEATHER_CHANCE  = 20 * 60 * 5;
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
     * Defines how far the citizen will be rendered.
     */
    public static final double RENDER_DISTANCE_WEIGHT     = 2.0D;
    /**
     * Building level at which the workers work even if it is raining.
     */
    public static final int    BONUS_BUILDING_LEVEL       = 5;
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
    public static final int    OFFSET_TICK_MULTIPLIER   = 7;
    /**
     * Range required for the citizen to be home.
     */
    public static final double RANGE_TO_BE_HOME         = 16;
    /**
     * If the entitiy is stuck for 2 minutes do something.
     */
    public static final int    MAX_STUCK_TIME           = 120;
    /**
     * The max amount of lines the latest log allows.
     */
    public static final int    MAX_LINES_OF_LATEST_LOG  = 4;
    /**
     * Distance from mobs the entity should hold.
     */
    public static final double DISTANCE_OF_ENTITY_AVOID = 5.0D;
    /**
     * Initital speed while running away from entities.
     */
    public static final double INITIAL_RUN_SPEED_AVOID  = 1.1D;
    /**
     * Later run speed while running away from entities.
     */
    public static final double LATER_RUN_SPEED_AVOID    = 0.8D;
    /**
     * The max range a citizen can call a guard to help-
     */
    public static final int    MAX_GUARD_CALL_RANGE     = 5000;
    /**
     * Happiness penalty for citizen death.
     */
    public static final double CITIZEN_DEATH_PENALTY    = 0.2;
    /**
     * Happiness penalty for citizen kill.
     */
    public static final double CITIZEN_KILL_PENALTY     = 9;
    /**
     * Big multiplier in extreme saturation situations.
     */
    public static final double BIG_SATURATION_FACTOR    = 0.05;
    /**
     * Small multiplier in average saturation situation.s
     */
    public static final double LOW_SATURATION_FACTOR    = 0.01;
    /**
     * Decrease by this * buildingLevel each new night.
     */
    public static final double SATURATION_DECREASE_FACTOR = 0.02;
    /**
     * Minimum stuck time for the worker to react.
     */
    public static final int    MIN_STUCK_TIME             = 5;

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
     * Distance between Barbarian and Citizen to not remove happiness.
     */
    public static final int BARB_DISTANCE_FOR_FREE_DEATH = 21;

    /**
     * The max citizen level.
     */
    public static final int MAX_CITIZEN_LEVEL = 99;

    /**
     * The Guard Building health modifier Name
     */
    public static final String GUARD_HEALTH_MOD_BUILDING_NAME = "MinecoloniesGuardBuildingHP";

    /**
     * The Config guard health modifier name
     */
    public static final String GUARD_HEALTH_MOD_CONFIG_NAME = "MinecoloniesGuardConfigHP";

    /**
     * The guard's level based health bonus mod's name
     */
    public static final String GUARD_HEALTH_MOD_LEVEL_NAME = "MinecoloniesGuardLevelHealth";

    /**
     * Eating particle count.
     */
    public static final int EATING_PARTICLE_COUNT = 5;

    /**
     * At this stack size or smaller the chance to dump is 50%.
     */
    public static final int CHANCE_TO_DUMP_50 = 16;

    /**
     * Chance to dump, if < this then dump else not.
     */
    public static final int CHANCE_TO_DUMP = 8;
}
