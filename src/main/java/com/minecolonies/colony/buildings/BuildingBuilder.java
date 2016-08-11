package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutBuilder;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.util.ServerUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingWorker
{
    private static final String BUILDER = "Builder";

    public BuildingBuilder(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return BUILDER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 2;
    }

    @Override
    public String getJobName()
    {
        return BUILDER;
    }

    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobBuilder(citizen);
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        final EntityPlayer owner = ServerUtils.getPlayerOnServerFromUUID(getColony().getPermissions().getOwner());

        if (newLevel == 1)
        {
            owner.triggerAchievement(ModAchievements.achBuildingBuilder);
        } else if (newLevel >= this.getMaxBuildingLevel())
        {
            owner.triggerAchievement(ModAchievements.achUpgradeBuilderMax);
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
            return new WindowHutBuilder(this);
        }
    }
}
