package com.minecolonies.colony.buildings;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.client.gui.WindowHutFisherman;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.util.ServerUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class BuildingFisherman extends AbstractBuildingWorker
{
    public BuildingFisherman(Colony c, BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName()
    {
        return "Fisherman";
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public String getJobName()
    {
        return "Fisherman";
    }

    @Override
    public AbstractJob createJob(CitizenData citizen)
    {
        return new JobFisherman(citizen);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
    }

    /**
     * 
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        EntityPlayer owner = ServerUtils.getPlayerOnServerFromUUID(this.getColony().getPermissions().getOwner());

        if (newLevel == 1)
        {
            owner.triggerAchievement(ModAchievements.achBuildingFisher);
        }
        else
            if (newLevel >= this.getMaxBuildingLevel())
            {
                owner.triggerAchievement(ModAchievements.achUpgradeFisherMax);
            }
    }

    public static class View extends AbstractBuildingWorker.View
    {
        public int[] levels;
        public int   current;

        public View(ColonyView c, BlockPos l)
        {
            super(c, l);
        }

        public com.blockout.views.Window getWindow()
        {
            return new WindowHutFisherman(this);
        }

        @Override
        public void deserialize(ByteBuf buf)
        {
            super.deserialize(buf);
        }
    }
}
