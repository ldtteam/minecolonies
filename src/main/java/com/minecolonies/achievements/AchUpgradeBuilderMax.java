package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.stats.Achievement;

/**
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchUpgradeBuilderMax extends AbstractAchievement
{
    public AchUpgradeBuilderMax(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutBuilder, ModAchievements.achBuildingBuilder);
    }
}
