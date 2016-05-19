package com.minecolonies.util;

/**
 * Utility class for experience calculation.
 */
public final class ExperienceUtils
{
    /**
     * The number to calculate the experienceLevel of the citizen.
     */
    private static final int EXPERIENCE_MULTIPLIER = 100;
    /**
     * The number to create a percentage from another number (ex. 100*0.25 = 25).
     */
    private static final int PERCENT_MULTIPLIER = 100;
    /**
     * Private constructor to hide the public one.
     */
    private ExperienceUtils()
    {
    }

    /**
     * Calculates the xp needed for the next level.
     * @param currentLevel the currentLevel of the citizen
     * @return the xp in int
     */
    public static int getXPNeededForNextLevel(int currentLevel)
    {
        return EXPERIENCE_MULTIPLIER * (currentLevel+1) * (currentLevel+1);
    }

    /**
     * Calculates how much percent of the current level have been completed
     * @param experience the current amount of xp
     * @param level the current level
     * @return the percentage
     */
    public static double getPercentOfLevelCompleted(double experience, int level)
    {
        return (experience - getXPNeededForNextLevel(level+1) / getXPNeededForNextLevel(level)) * PERCENT_MULTIPLIER;
    }


}
