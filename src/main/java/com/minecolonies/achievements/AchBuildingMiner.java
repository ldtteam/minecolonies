package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

public class AchBuildingMiner extends AbstractAchievement
{
    
    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchBuildingMiner(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutMiner, ModAchievements.achBuildingTownhall);
    }

}
