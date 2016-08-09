package com.minecolonies.achievements;

import net.minecraft.stats.Achievement;

/**
 * The achievement page
 * 
 * @author Isfirs
 * @since 0.1
 */
public class AchievementPageMineColonies extends AbstractAchievementPage
{
    
    /**
     * Constructor
     * 
     * @param achievements
     */
    public AchievementPageMineColonies(final Achievement... achievements)
    {
        super("MineColonies", achievements);
    }
}
