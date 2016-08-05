package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.1
 */
public class AchSizeTown extends AbstractSizeAchievement {

	public AchSizeTown(String id, String name, int offsetX, int offsetY, int size) {
		super(id, name, offsetX, offsetY, Items.diamond, ModAchievements.achSizeSettlement, size);
	}
}
