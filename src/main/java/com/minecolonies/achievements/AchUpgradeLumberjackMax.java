package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchUpgradeLumberjackMax extends AbstractAchievement
{
    public AchUpgradeLumberjackMax(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutLumberjack, ModAchievements.achBuildingLumberjack);
    }
}
