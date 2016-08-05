package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.stats.Achievement;

/**
 * Achievement: My very first building
 * Granted for: Build a builder hut
 *
 * @author Isfirs
 * @since 0.1
 */
public class AchBuilder extends AbstractAchievement
{
    public AchBuilder(final String id,
                      final String name,
                      final int offsetX,
                      final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutBuilder, ModAchievements.achTownhall);
    }
}
