package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.entity.ai.EntityAIWorkDeliveryman;
import com.minecolonies.util.BlockPosUtil;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class JobDeliveryman extends Job
{
    private                 BlockPos            destination;

    private static final    String              TAG_DESTINATION = "destination";

    public JobDeliveryman(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.job.Deliveryman";
    }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.DELIVERYMAN;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (hasDestination())
        {
            BlockPosUtil.writeToNBT(compound, TAG_DESTINATION, destination);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey(TAG_DESTINATION))
        {
            destination = BlockPosUtil.readFromNBT(compound, TAG_DESTINATION);
        }
    }

    public boolean isNeeded()
    {
        Colony colony = getCitizen().getColony();
        return colony != null && !colony.getDeliverymanRequired().isEmpty();
    }

    @Override
    public void addTasks(EntityAITasks tasks)
    {
        tasks.addTask(3, new EntityAIWorkDeliveryman(this));
    }

    /**
     * Returns whether or not the job has a destination
     *
     * @return          true if has destination, otherwise false
     */
    public boolean hasDestination()
    {
        return destination != null;
    }

    /**
     * Returns the {@link BlockPos} of the destination
     *
     * @return          {@link BlockPos} of the destination
     */
    public BlockPos getDestination()
    {
        return destination;
    }

    /**
     * Sets the destination of the job
     *
     * @param destination   {@link BlockPos} of the destination
     */
    public void setDestination(BlockPos destination)
    {
        this.destination = destination;
    }
}
