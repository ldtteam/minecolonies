package com.minecolonies.achievements;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.1
 */
public class AchSizeTown extends AbstractSizeAchievement {
    
    public static final int size = 10;

	public AchSizeTown(String id, String name, int offsetX, int offsetY, int size) {
		super(id, name, offsetX, offsetY, Items.diamond, ModAchievements.achSizeSettlement, size);
	}
	
    public void triggerAchievement(EntityPlayer player, int size)
    {
        if (this.compare(size)) {
            player.triggerAchievement(this);
        }
    }
}
