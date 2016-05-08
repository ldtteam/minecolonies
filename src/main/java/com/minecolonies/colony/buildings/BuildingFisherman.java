package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutFisherman;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.colony.jobs.JobFisherman;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class BuildingFisherman extends BuildingWorker
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
    public Job createJob(CitizenData citizen)
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

    public static class View extends BuildingWorker.View
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

