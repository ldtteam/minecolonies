package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 * Achievement: My very first building Granted for: Build a builder hut
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchievementBuildingBuilder extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchievementBuildingBuilder(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutBuilder, ModAchievements.achievementBuildingTownhall);
    }
}
