package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIWorkDeliveryman;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class EntityDeliveryman extends EntityWorker
{
    private ChunkCoordinates destination;

    public EntityDeliveryman(World world)
    {
        super(world);
    }

    @Override
    protected void initTasks()
    {
        super.initTasks();
        this.tasks.addTask(3, new EntityAIWorkDeliveryman(this));
    }

    @Override
    protected String initJob()
    {
        return "Deliveryman";
    }

    @Override
    public int getTextureID()//TODO: add female texture (and more textures?)
    {
        return 1;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        if(hasDestination())
        {
            ChunkCoordUtils.writeToNBT(compound, "destination", destination);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        if(compound.hasKey("destination"))
        {
            destination = ChunkCoordUtils.readFromNBT(compound, "destination");
        }
    }

    @Override
    public boolean isNeeded()
    {
        return getTownHall() != null && !getTownHall().getDeliverymanRequired().isEmpty();
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
