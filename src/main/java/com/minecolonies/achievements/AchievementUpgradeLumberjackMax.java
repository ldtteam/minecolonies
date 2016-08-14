package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchievementUpgradeLumberjackMax extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchievementUpgradeLumberjackMax(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutLumberjack, ModAchievements.achievementBuildingLumberjack);
    }
}
