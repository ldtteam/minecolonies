package com.minecolonies.achievements;

import net.minecraft.init.Items;

/**
 * 
 * @author Isfirs
 * @since 0.1
 */
public class AchSizeCity extends AbstractAchievement {

	public AchSizeCity(String id, String name, int offsetX, int offsetY, int size) {
		super(id, name, offsetX, offsetY, Items.gold_ingot, ModAchievements.achSizeTown);
	}
}
