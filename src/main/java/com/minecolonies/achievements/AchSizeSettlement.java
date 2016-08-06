package com.minecolonies.achievements;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.1
 */
public class AchSizeSettlement extends AbstractSizeAchievement
{
    
    public static final int size = 10;
    
    public AchSizeSettlement(final String id,
                       final String name,
                       final int offsetX,
                       final int offsetY,
                       final int size)
    {
        super(id, name, offsetX, offsetY, Items.iron_ingot, null, size);
    }
    
    @Override
    public void triggerAchievement(EntityPlayer player, int size)
    {
        if (this.compare(size)) {
            player.triggerAchievement(this);
        }
    }
}
