package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutFisherman;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.util.ServerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

/**
 * The fishermans building.
 */
public class BuildingFisherman extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 3;
    /**
     * The job description.
     */
    private static final String FISHERMAN          = "Fisherman";

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingFisherman(Colony c, BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter of the schematic name.
     *
     * @return the schematic name.
     */
    @Override
    public String getSchematicName()
    {
        return FISHERMAN;
    }

    /**
     * Getter of the max building level.
     *
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * Getter of the job description.
     *
     * @return the description of the fisherman job.
     */
    @Override
    public String getJobName()
    {
        return FISHERMAN;
    }

    /**
     * Create the job for the fisherman.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobFisherman(citizen);
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        final EntityPlayer owner = ServerUtils.getPlayerFromUUID(this.getColony().getPermissions().getOwner());

        if (newLevel == 1)
        {
            owner.triggerAchievement(ModAchievements.achievementBuildingFisher);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            owner.triggerAchievement(ModAchievements.achievementUpgradeFisherMax);
        }
    }

    /**
     * Provides a view of the fisherman building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        /**
         * Gets the blockOut Window.
         *
         * @return the window of the fisherman building.
         */
        @Override
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutFisherman(this);
        }

    }
}
