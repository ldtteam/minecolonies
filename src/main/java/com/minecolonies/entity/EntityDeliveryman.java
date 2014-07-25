package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIGoHome;
import com.minecolonies.entity.ai.EntityAISleep;
import com.minecolonies.entity.ai.EntityAIWorkDeliveryman;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityDeliveryman extends EntityWorker
{
    private Vec3 destination;

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
            Vec3Utils.writeVecToNBT(compound, "destination", destination);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        if(compound.hasKey("destination")) destination = Vec3Utils.readVecFromNBT(compound, "destination");
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

    public Vec3 getDestination()
    {
        return destination;
    }

    public void setDestination(Vec3 destination)
    {
        this.destination = destination;
    }
}
