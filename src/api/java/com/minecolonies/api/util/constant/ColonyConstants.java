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
     * Default average randomization for onWorldTick() methods
    */
    public static final int ONWORLD_TICK_AVERAGE = 1 * TICKS_SECOND;

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
     * Average happiness of a citizen.
     */
    public static final double AVERAGE_HAPPINESS = 5.0;

    /**
     * Max overall happiness.
     */
    public static final double MAX_OVERALL_HAPPINESS = 10;

    /**
     * Min overall happiness.
     */
    public static final double MIN_OVERALL_HAPPINESS = 1;

    /**
     * Amount of ticks to wait until checking if a waypoint is still valid.
     */
    public static final int    CHECK_WAYPOINT_EVERY              = 100;

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
     * Overall hapiness when the colony is started.
     */
    public static final int DEFAULT_OVERALL_HAPPYNESS = 5;

    /**
     * Max amount of permission events to store in the colony.
     */
    public static final int MAX_PERMISSION_EVENTS = 100;

    /**
     * Barbarian Constants.
     */
    public static final ResourceLocation BARBARIAN                    = new ResourceLocation(Constants.MOD_ID, "Barbarian");
    public static final ResourceLocation ARCHER                       = new ResourceLocation(Constants.MOD_ID, "ArcherBarbarian");
    public static final ResourceLocation CHIEF                        = new ResourceLocation(Constants.MOD_ID, "ChiefBarbarian");
    public static final int              MAX_SIZE                     = Configurations.gameplay.maxBarbarianHordeSize;
    public static final double           BARBARIANS_MULTIPLIER        = 0.5;
    public static final double           ARCHER_BARBARIANS_MULTIPLIER = 0.25;
    public static final double           CHIEF_BARBARIANS_MULTIPLIER  = 0.1;
    public static final int              PREFERRED_MAX_HORDE_SIZE     = 40;
    public static final int              PREFERRED_MAX_BARBARIANS     = 22;
    public static final int              PREFERRED_MAX_ARCHERS        = 16;
    public static final int              PREFERRED_MAX_CHIEFS         = 2;
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
     * Private constructor to hide the implicit one.
     */
    private ColonyConstants()
    {
        /**
         * Intentionally left empty.
         */
    }
}
