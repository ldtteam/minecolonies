package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.2
 */
public class AchSizeTown extends AbstractSizeAchievement
{

    public static final int SIZE = 20;

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     */
    public AchSizeTown(String id, String name, int offsetX, int offsetY)
    {
        super(id, name, offsetX, offsetY, Items.diamond, ModAchievements.achSizeSettlement);
    }

}
