package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchievementSizeSettlement extends AbstractSizeAchievement
{

    public static final int SIZE = 5;

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchievementSizeSettlement(final String id, final String name, final int offsetX, final int offsetY)
    {
        super(id, name, offsetX, offsetY, Items.iron_ingot, null);
    }

}
