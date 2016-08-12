package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 *
 * @author Isfirs
 * @since 0.2
 */
public class AchUpgradeFisherMax extends AbstractAchievement
{
    public AchUpgradeFisherMax(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutFisherman, ModAchievements.achBuildingFisher);
    }
}
