package com.minecolonies.api.util.constant;

/**
 * Class for happiness constants.
 */
public final class HappinessConstants
{
    /**
     * constants for house modifier.
     */
    public static final int COMPLAIN_DAYS_WITHOUT_HOUSE = 7;
    public static final int DEMANDS_DAYS_WITHOUT_HOUSE = 14;

    public static final int COMPLAIN_DAYS_SICK = 7;
    public static final int DEMANDS_CURE_SICK = 14;

    /**
     * constants for job modifier.
     */
    public static final int COMPLAIN_DAYS_WITHOUT_JOB = 7;
    public static final int DEMANDS_DAYS_WITHOUT_JOB = 14;

    /**
     * constants for happiness min/max and start happines values.
     */
    public static final int MAX_HAPPINESS = 10;

    /**
     * constants for no tools.
     */
    public static final int IDLE_AT_JOB_COMPLAINS_DAYS = 7;
    public static final int IDLE_AT_JOB_DEMANDS_DAYS   = 14;

    /**
     * Private constructor to hide implicit public one.
     */
    private HappinessConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
