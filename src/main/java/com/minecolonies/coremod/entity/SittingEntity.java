package com.minecolonies.coremod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Entity used to sit on, for animation purposes.
 */
public class SittingEntity extends Entity
{
    /**
     * The lifetime in ticks of the entity, auto-dismounts after.
     */
    int maxLifeTime = 100;

    public SittingEntity(final EntityType type, final World worldIn)
    {
        super(type, worldIn);

        this.setInvisible(true);
        this.forceSpawn = true;
        this.noClip = true;
        this.setNoGravity(true);
    }

    public SittingEntity(final EntityType type, final World worldIn, double x, double y, double z, int lifeTime)
    {
        super(type, worldIn);

        this.setPosition(x, y, z);

        this.setInvisible(true);
        this.forceSpawn = true;
        this.noClip = true;
        this.setNoGravity(true);
        this.maxLifeTime = lifeTime;
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
    protected void readAdditional(final CompoundNBT compound)
    {

    }

    @Override
    protected void writeAdditional(final CompoundNBT compound)
    {

    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerData()
    {

    }

    @Override
    public void tick()
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
            this.remove();
        }
    }

    /**
     * Sets the lifetime
     *
     * @param maxLifeTime the max life span of the entity.
     */
    public void setMaxLifeTime(final int maxLifeTime)
    {
        this.maxLifeTime = maxLifeTime;
    }
}
