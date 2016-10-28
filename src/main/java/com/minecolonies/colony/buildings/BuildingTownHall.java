package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowTownHall;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BuildingTownHall extends AbstractBuildingHut
{
    private static final String TOWN_HALL = "TownHall";

    public BuildingTownHall(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return TOWN_HALL;
    }

    public static class View extends AbstractBuildingHut.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public com.blockout.views.Window getWindow()
        {
            return new WindowTownHall(this);
        }
    }
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingTownhall);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeTownhallMax);
        }
    }
}
