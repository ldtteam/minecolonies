package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

/**
 * Achievement: A new Dawn
 * Granted for: Placing the Townhall
 *
 * @author Isfirs
 * @since 0.1
 */
public class AchBuildingTownhall extends AbstractAchievement
{
    public AchBuildingTownhall(final String id,
                       final String name,
                       final int offsetX,
                       final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutTownHall, ModAchievements.achGetSupply);
    }
}
