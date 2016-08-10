package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchSizeCity extends AbstractSizeAchievement
{

    public static final int size = 10;

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param size
     */
    public AchSizeCity(String id, String name, int offsetX, int offsetY, int size)
    {
        super(id, name, offsetX, offsetY, Items.gold_ingot, ModAchievements.achSizeTown, size);
    }

}
