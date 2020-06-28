package com.minecolonies.coremod.util;

/**
 * Utility class for experience calculation.
 */
public final class ExperienceUtils
{
    /**
     * The number to calculate the experienceLevel of the citizen.
     */
    private static final double EXPERIENCE_MULTIPLIER = 1D;

    /**
     * The number to create a percentage from another number (ex. 100*0.25 =
     * 25).
     */
    private static final double PERCENT_MULTIPLIER = 100D;

    /**
     * Private constructor to hide the public one.
     */
    private ExperienceUtils()
    {
    }

    /**
     * Calculates how much percent of the current level have been completed.
     *
     * @param experience the current amount of xp.
     * @param level      the current level.
     * @return the percentage.
     */
    public static double getPercentOfLevelCompleted(final double experience, final int level)
    {
        final double thisLvlExp = getXPNeededForOnlyLevel(level);
        final double lastLvlExp = getXPNeededForNextLevel(level) - thisLvlExp;
        final double currentExp = experience - lastLvlExp;

        return Math.min(PERCENT_MULTIPLIER,
          PERCENT_MULTIPLIER
            - ((thisLvlExp
                  - currentExp)
                 / thisLvlExp)
                * PERCENT_MULTIPLIER);
    }

    /**
     * Calculates the xp needed for the next level.
     *
     * @param currentLevel the currentLevel of the citizen
     * @return the xp in int
     */
    private static double getXPNeededForOnlyLevel(final int currentLevel)
    {
        if (currentLevel == 0)
        {
            return getXPNeededForNextLevel(currentLevel);
        }
        return getXPNeededForNextLevel(currentLevel)
                 - getXPNeededForNextLevel(currentLevel - 1);
    }

    /**
     * Calculates the xp needed for the next level.
     *
     * @param currentLevel the currentLevel of the citizen
     * @return the xp in int
     */
    public static double getXPNeededForNextLevel(final int currentLevel)
    {
        return EXPERIENCE_MULTIPLIER
                 * (currentLevel + 1)
                 * (currentLevel + 1);
    }

    /**
     * Calculate level depending on the experience.
     * @param xp the experience to calculate it for.
     * @return the correct level.
     */
    public static int calculateLevel(final double xp)
    {
        int startLevel = 0;
        while (getXPNeededForNextLevel(startLevel) < xp)
        {
            startLevel++;
        }
        return startLevel;
    }
}
