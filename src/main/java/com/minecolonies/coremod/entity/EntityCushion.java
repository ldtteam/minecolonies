package com.minecolonies.coremod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Dummy Entity used to allow citizen to sit on a block.
 * These entities are removed from the world as soon as
 * the citizen stands up.
 * 
 * @author kevin
 *
 */
public class EntityCushion extends Entity
{
    protected double mountedYOffset = 2.2;
    protected boolean citizenSitting;
    
    /**
     * @param worldIn
     * @param mountHeight
     */
    public EntityCushion(World worldIn, double mountHeight)
    {
        super(worldIn);
        mountedYOffset = mountHeight;
        setSize(0.01f, 0.01f);
        citizenSitting = false;
    }

    /**
     * @param worldIn
     */
    public EntityCushion(World worldIn) {
        this(worldIn, 0);
    }
    
    
    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return !citizenSitting;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return true;
    }

    @Override
    public double getMountedYOffset() {
        return mountedYOffset;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        citizenSitting = false;
        setDead();
    }
    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        mountedYOffset = tagCompund.getDouble("height");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setDouble("height", mountedYOffset);
    }
    
    /**
     * call to tell this entity is being riden.
     */
    public void setSeatTaken()
    {
        citizenSitting = true;
    }
    
    /**
     * Indicates if the seat is being used or not.
     *
     * @return Return true if seat is being used.
     */
    public boolean isSeatTaken()
    {
        return citizenSitting;
    }
}
