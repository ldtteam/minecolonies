package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.1
 */
public class AchSizeSettlement extends AbstractSizeAchievement
{
    public AchSizeSettlement(final String id,
                       final String name,
                       final int offsetX,
                       final int offsetY,
                       final int size)
    {
        super(id, name, offsetX, offsetY, Items.iron_ingot, null, size);
    }
}
