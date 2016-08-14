package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchUpgradeMinerMax extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchUpgradeMinerMax(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutMiner, ModAchievements.achBuildingMiner);
    }
}
