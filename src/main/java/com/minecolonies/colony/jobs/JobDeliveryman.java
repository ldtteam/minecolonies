package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.entity.ai.EntityAIWorkDeliveryman;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class JobDeliveryman extends Job
{
    private ChunkCoordinates destination;

    private static final String TAG_DESTINATION = "destination";

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
            ChunkCoordUtils.writeToNBT(compound, TAG_DESTINATION, destination);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey(TAG_DESTINATION))
        {
            destination = ChunkCoordUtils.readFromNBT(compound, TAG_DESTINATION);
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

    public boolean hasDestination()
    {
        return destination != null;
    }

    public ChunkCoordinates getDestination()
    {
        return destination;
    }

    public void setDestination(ChunkCoordinates destination)
    {
        this.destination = destination;
    }
}
