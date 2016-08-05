package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

public class AchBuildingLumberjack extends AbstractAchievement {

	public AchBuildingLumberjack(String id, String name, int offsetX, int offsetY) {
		super(id, name, offsetX, offsetY, ModBlocks.blockHutLumberjack, ModAchievements.achBuildingTownhall);
	}

}
