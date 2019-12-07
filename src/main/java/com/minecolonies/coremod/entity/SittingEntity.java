package com.minecolonies.coremod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * Entity used to sit on, for animation purposes.
 */
public class SittingEntity extends Entity
{
    /**
     * The lifetime in ticks of the entity, auto-dismounts after.
     */
    int maxLifeTime = 100;

    public SittingEntity(final World worldIn)
    {
        super(worldIn);
        this.setSize(0F, 0.5F);
        this.setInvisible(true);
        this.forceSpawn = true;
        this.noClip = false;
        this.setNoGravity(true);
    }

    public SittingEntity(final World worldIn, double x, double y, double z, int lifeTime)
    {
        super(worldIn);

        this.setPosition(x, y, z);
        this.setSize(0F, 0.5F);
        this.setInvisible(true);
        this.forceSpawn = true;
        this.noClip = false;
        this.setNoGravity(true);
        this.maxLifeTime = lifeTime;
    }

    @Override
    protected void entityInit()
    {

    }

    /**
     * Do not let the entity be destroyed
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    /**
     * No Collision
     */
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound nbtTagCompound)
    {

    }

    @Override
    public void onUpdate()
    {
        if (this.world.isRemote)
        {
            return;
        }

        if (!this.isBeingRidden() || maxLifeTime-- < 0)
        {
            if (getPassengers().size() > 0)
            {
                Entity e = getPassengers().get(0);
                this.removePassengers();
                e.setPosition(this.posX, this.posY + 1, this.posZ);
            }
            this.setDead();
        }
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound nbtTagCompound)
    {

    }
}
