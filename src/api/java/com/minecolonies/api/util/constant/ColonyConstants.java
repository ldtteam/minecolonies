package com.minecolonies.api.util.constant;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.MathUtils;
import net.minecraft.util.ResourceLocation;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * Colony wide constants.
 */
public final class ColonyConstants
{
    //  Settings
    public static final int CLEANUP_TICK_INCREMENT = 5 * TICKS_SECOND;

    /**
     * The colony name in the team + the id.
     */
    public static final String TEAM_COLONY_NAME = "teamcolony";

    /**
     * Default average randomization for onWorldTick() methods
    */
    public static final int ONWORLD_TICK_AVERAGE = TICKS_SECOND;

    public static final int NUM_ACHIEVEMENT_FIRST  = 1;
    public static final int NUM_ACHIEVEMENT_SECOND = 25;
    public static final int NUM_ACHIEVEMENT_THIRD  = 100;
    public static final int NUM_ACHIEVEMENT_FOURTH = 500;
    public static final int NUM_ACHIEVEMENT_FIFTH  = 1000;

    /**
     * The default spawn radius required for barbarians.
     */
    public static final int DEFAULT_SPAWN_RADIUS = 10;

    /**
     * Max spawn radius of the barbarians.
     */
    public static final int MAX_SPAWN_RADIUS = 75;

    /**
     * Bonus happiness each factor added.
     */
    public static final double HAPPINESS_FACTOR = 0.1;

    /**
     * Saturation at which a citizen starts being happy.
     */
    public static final int WELL_SATURATED_LIMIT = 5;

    /**
     * Amount of ticks to wait until checking if a waypoint is still valid.
     */
    public static final int    CHECK_WAYPOINT_EVERY              = 100;

    /**
     * How often the subscribers get updated in ticks.
     */
    public static final int UPDATE_SUBSCRIBERS_INTERVAL = 20;

    /**
     * How often the colony state gets updated in ticks.
     */
    public static final int UPDATE_STATE_INTERVAL   = 100;
    /**
     * How often the colony request system gets updated in ticks.
     */
    public static final int UPDATE_RS_INTERVAL      = 11;
    /**
     * How often the colony updates day/nighttime in ticks.
     */
    public static final int UPDATE_DAYTIME_INTERVAL = 20;

    /**
     * Distance of when to add new subscribers.
     */
    public static final double MAX_SQ_DIST_SUBSCRIBER_UPDATE     = MathUtils.square(Configurations.gameplay.workingRangeTownHall + 16D);

    /**
     * Distance of when to remove old subscribers.
     */
    public static final double MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE = MathUtils.square(Configurations.gameplay.workingRangeTownHall * 2D);

    /**
     * Size of a chunk.
     */
    public static final int CONST_CHUNKSIZE = 16;

    /**
     * Max amount of permission events to store in the colony.
     */
    public static final int MAX_PERMISSION_EVENTS = 100;

    /**
     * Barbarian Constants.
     */
    public static final ResourceLocation BARBARIAN                    = new ResourceLocation(Constants.MOD_ID, "Barbarian");
    public static final ResourceLocation MERCENARY                    = new ResourceLocation(Constants.MOD_ID, "Mercenary");
    public static final ResourceLocation ARCHER                       = new ResourceLocation(Constants.MOD_ID, "ArcherBarbarian");
    public static final ResourceLocation CHIEF                        = new ResourceLocation(Constants.MOD_ID, "ChiefBarbarian");
    public static final double           ARCHER_BARBARIANS_MULTIPLIER = 0.30;
    public static final double           CHIEF_BARBARIANS_MULTIPLIER  = 0.1;
    public static final int              MIN_CITIZENS_FOR_RAID        = 5;
    public static final int              NUMBER_OF_CITIZENS_NEEDED    = 5;


    /**
     * Different horde ids and their sizes.
     */
    public static final int              SMALL_HORDE_MESSAGE_ID       = 1;
    public static final int              MEDIUM_HORDE_MESSAGE_ID      = 2;
    public static final int              BIG_HORDE_MESSAGE_ID         = 3;
    public static final int              HUGE_HORDE_MESSAGE_ID        = 4;
    public static final int              SMALL_HORDE_SIZE             = 5;
    public static final int              MEDIUM_HORDE_SIZE            = 10;
    public static final int              BIG_HORDE_SIZE               = 20;

    /**
     * Pirate Constants.
     */
    public static final ResourceLocation PIRATE        = new ResourceLocation(Constants.MOD_ID, "Pirate");
    public static final ResourceLocation PIRATE_ARCHER = new ResourceLocation(Constants.MOD_ID, "ArcherPirate");
    public static final ResourceLocation PIRATE_CHIEF  = new ResourceLocation(Constants.MOD_ID, "ChiefPirate");

    public static final String SMALL_PIRATE_SHIP = "small_pirate_ship";
    public static final String MEDIUM_PIRATE_SHIP = "medium_pirate_ship";
    public static final String BIG_PIRATE_SHIP = "big_pirate_ship";

    /**
     * Turn off the help manager when one of these applies.
     */
    public static final int CITIZEN_LIMIT_FOR_HELP  = 20;
    public static final int BUILDING_LIMIT_FOR_HELP = 10;

    /**
     * Private constructor to hide the implicit one.
     */
    private ColonyConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
