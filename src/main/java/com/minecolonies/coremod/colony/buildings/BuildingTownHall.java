package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowTownHall;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to manage the townHall building block.
 */
public class BuildingTownHall extends AbstractBuildingHut
{
    /**
     * Description of the block used to set this block.
     */
    private static final String TOWN_HALL = "TownHall";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingTownHall(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return TOWN_HALL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementBuildingTownhall);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementUpgradeTownhallMax);
        }
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingHut.View
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public Window getWindow()
        {
            return new WindowTownHall(this);
        }
    }
}
