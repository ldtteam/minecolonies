package com.minecolonies.api.util.constant;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.MathUtils;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * Colony wide constants.
 */
public final class ColonyConstants
{
    //  Settings
    public static final int CLEANUP_TICK_INCREMENT = 5 * TICKS_SECOND;

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

    public static final int    CHECK_WAYPOINT_EVERY              = 100;
    public static final double MAX_SQ_DIST_SUBSCRIBER_UPDATE     = MathUtils.square(Configurations.Gameplay.workingRangeTownHall + 16D);
    public static final double MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE = MathUtils.square(Configurations.Gameplay.workingRangeTownHall * 2D);

    public static final int CONST_CHUNKSIZE = 16;
    public static final int DEFAULT_OVERALL_HAPPYNESS = 5;

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
