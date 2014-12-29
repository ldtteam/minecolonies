package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.EntityAIWorkMiner;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;

public class JobMiner extends Job
{
    public JobMiner(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Miner"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.MINER;
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

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkMiner(this));
    }
}
