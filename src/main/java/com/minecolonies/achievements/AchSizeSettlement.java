package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;

/**
 * Created by PascalBahl on 03.08.16.
 */
public class AchPioneers extends AbstractAchievement
{
    public AchPioneers(final String id,
                       final String name,
                       final int offsetX,
                       final int offsetY)
    {
        super(id, name, offsetX, offsetY, ModBlocks.blockHutBuilder, null);
    }
}
