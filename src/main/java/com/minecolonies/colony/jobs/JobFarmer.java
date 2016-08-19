package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import net.minecraft.nbt.NBTTagCompound;

public class JobFarmer extends AbstractJob
{
    public JobFarmer(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName(){ return "com.minecolonies.job.Farmer"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.FARMER;
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
     * Override to add Job-specific AI tasks to the given EntityAITask list
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkFarmer(this);
    }
}
