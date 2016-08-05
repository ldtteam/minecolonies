package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

public class AchBuildingLumberjack extends AbstractAchievement {

	public AchBuildingLumberjack(String id, String name, int offsetX, int offsetY) {
		super(id, name, offsetX, offsetY, ModBlocks.blockHutLumberjack, ModAchievements.achBuildingTownhall);
	}

}
