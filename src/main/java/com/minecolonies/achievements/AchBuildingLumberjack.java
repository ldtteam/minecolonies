package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchBuildingLumberjack extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchBuildingLumberjack(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutLumberjack, ModAchievements.achBuildingTownhall);
    }

}
