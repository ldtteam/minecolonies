package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchievementSizeCity extends AbstractSizeAchievement
{

    public static final int SIZE = 10;

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchievementSizeCity(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, Items.gold_ingot, ModAchievements.achSizeTown);
    }

}
