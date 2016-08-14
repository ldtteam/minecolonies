package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchSizeCity extends AbstractSizeAchievement
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
    public AchSizeCity(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, Items.gold_ingot, ModAchievements.achSizeTown);
    }

}
