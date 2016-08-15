package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.util.ServerUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public class BuildingLumberjack extends AbstractBuildingWorker
{
    private static final String LUMBERJACK          = "Lumberjack";
    private static final String LUMBERJACK_HUT_NAME = "lumberjackHut";

    public BuildingLumberjack(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return LUMBERJACK;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public String getJobName()
    {
        return LUMBERJACK;
    }

    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    /**
     * 
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        final EntityPlayer owner = ServerUtils.getPlayerFromUUID(getColony().getPermissions().getOwner());

        if (newLevel == 1)
        {
            owner.triggerAchievement(ModAchievements.achievementBuildingLumberjack);
        }
        else
            if (newLevel >= this.getMaxBuildingLevel())
            {
                owner.triggerAchievement(ModAchievements.achievementUpgradeLumberjackMax);
            }
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, LUMBERJACK_HUT_NAME);
        }
    }

}
