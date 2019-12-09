package com.minecolonies.api.util.constant;

/**
 * Class for happiness constants.
 */
public final class HappinessConstants
{
    /**
     * constants for house modifier.
     */
    public static final int MAX_DAYS_WITHOUT_HOUSE = 30;
    public static final int MAX_HOUSE_PENALTY = 5;
    public static final double HOUSE_MODIFIER_POSITIVE = 0.5;
    public static final int COMPLAIN_DAYS_WITHOUT_HOUSE = 7;
    public static final int DEMANDS_DAYS_WITHOUT_HOUSE = 14;

    /**
     * constants for job modifier.
     */
    public static final int MAX_DAYS_WITHOUT_JOB = 30;
    public static final int COMPLAIN_DAYS_WITHOUT_JOB = 7;
    public static final int DEMANDS_DAYS_WITHOUT_JOB = 14;
    public static final int MAX_JOB_PENALTY = 5;
    public static final double JOB_MODIFIER_POSITIVE = 0.5;

    /**
     * constants for food modifier.
     */
    public static final int FOOD_MODIFIER_MAX = -2;
    public static final int FOOD_MODIFIER_MIN = -1;
    public static final double FOOD_MODIFIER_POSITIVE = 0.5;

    /**
     * constants for field modifiers
     */
    public static final int FIELD_MAX_DAYS_MODIFIER = 30;
    public static final double FIELD_MODIFIER_MAX = -0.75;
    public static final double FIELD_MODIFIER_MIN = -0.15;
    public static final double FIELD_MODIFIER_POSITIVE = 0.2;
    public static final double NO_FIELD_MODIFIER = -3.0;
    public static final int NO_FIELDS_COMPLAINS_DAYS = 7;

    /**
     * constants for damage modifiers
     */
    public static final int DAMAGE_MODIFIER_MAX = -2;
    public static final int DAMAGE_MODIFIER_MID = -1;
    public static final double DAMAGE_MODIFIER_MIN = -0.5;
    public static final double DAMAGE_LOWEST_POINT = 0.25d;
    public static final double DAMAGE_MEDIUM_POINT = 0.50d;
    public static final double DAMAGE_HIGHEST_POINT = 0.75d;

    /**
     * constants for happiness min/max and start happines values.
     */
    public static final int MAX_HAPPINESS = 10;
    public static final int MIN_HAPPINESS = 1;
    public static final int BASE_HAPPINESS = 8;

    /**
     * constants for no tools.
     */
    public static final int NO_TOOLS_MODIFIER = 3;
    public static final int NO_TOOLS_COMPLAINS_DAYS = 7;
    public static final int NO_TOOLS_DEMANDS_DAYS = 14;
    public static final int NO_TOOLS_MAX_DAYS_MODIFIER = 30;

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
