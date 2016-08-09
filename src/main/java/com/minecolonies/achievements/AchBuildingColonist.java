package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

public class AchBuildingColonist extends AbstractAchievement
{
    
    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchBuildingColonist(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutCitizen, ModAchievements.achBuildingTownhall);
    }

}
