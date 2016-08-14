package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchievementBuildingColonist extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchievementBuildingColonist(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutCitizen, ModAchievements.achBuildingTownhall);
    }

}
