package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.stats.Achievement;

/**
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchBuildingFisher extends AbstractAchievement
{

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchBuildingFisher(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutFisherman, ModAchievements.achBuildingTownhall);
    }
}
