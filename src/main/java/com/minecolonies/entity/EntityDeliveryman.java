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
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityMob.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(2, new EntityAIGoHome(this));
        this.tasks.addTask(3, new EntityAISleep(this));
        this.tasks.addTask(3, new EntityAIWorkDeliveryman(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(6, new EntityAIWatchClosest2(this, EntityCitizen.class, 5.0F, 0.02F));
        this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F));
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
        return getTownHall() != null && !getTownHall().getCitizens().isEmpty();
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
