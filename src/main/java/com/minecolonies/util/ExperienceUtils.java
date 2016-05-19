package com.minecolonies.util;

import static io.netty.util.ResourceLeakDetector.getLevel;

/**
 * Created by ray on 19.05.16.
 */
public final class ExperienceUtils
{
    /**
     * The number to calculate the experienceLevel of the citizen
     */
    private static final int EXPERIENCE_MULTIPLIER = 100;

    /**
     * Calculates the xp needed for the next level
     * @return the xp in double
     */
    public static int getXPNeededForNextLevel(int currentLevel)
    {
        return EXPERIENCE_MULTIPLIER * (currentLevel+1) * (currentLevel+1);
    }



}
