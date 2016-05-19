package com.minecolonies.util;

public final class ExperienceUtils
{
    /**
     * The number to calculate the experienceLevel of the citizen
     */
    private static final int EXPERIENCE_MULTIPLIER = 100;

    /**
     * Private constructor to hide the public one
     */
    private ExperienceUtils()
    {
    }

    /**
     * Calculates the xp needed for the next level
     * @param currentLevel the currentLevel of the citizen
     * @return the xp in int
     */
    public static int getXPNeededForNextLevel(int currentLevel)
    {
        return EXPERIENCE_MULTIPLIER * (currentLevel+1) * (currentLevel+1);
    }



}
