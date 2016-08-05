package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;

public class AchBuildingColonist extends AbstractAchievement {


	public AchBuildingColonist(String id, String name, int offsetX, int offsetY) {
		super(id, name, offsetX, offsetY, ModBlocks.blockHutCitizen, ModAchievements.achBuildingTownhall);
	}

}
