package com.minecolonies.coremod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy Entity used to allow citizen to sit on a block.
 * These entities are removed from the world as soon as
 * the citizen stands up.
 */
public class EntitySitable extends Entity
{
    /**
     * The y offset if mounted.
     */
    private double  mountedYOffset;

    /**
     * If a citizen is sitting on it.
     */
    private boolean citizenSitting;

    /**
     * Creates a new entity which can be sat on.
     * @param worldIn the world.
     * @param mountHeight the desired mount height.
     */
    public EntitySitable(World worldIn, double mountHeight)
    {
        super(worldIn);
        mountedYOffset = mountHeight;
        setSize(0.01f, 0.01f);
        citizenSitting = false;
    }

    /**
     * Constructor for creation, don't delete.
     * @param worldIn the world.
     */
    public EntitySitable(World worldIn)
    {
        this(worldIn, 0);
    }

        @Override
    protected boolean canBeRidden(Entity entityIn)
    {
        return !citizenSitting;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger)
    {
        return true;
    }

    @Override
    public double getMountedYOffset()
    {
        return mountedYOffset;
    }

    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        citizenSitting = false;
        setDead();
    }

    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    protected void readEntityFromNBT(@NotNull final NBTTagCompound tagCompund)
    {
        mountedYOffset = tagCompund.getDouble("height");
    }

    @Override
    protected void writeEntityToNBT(@NotNull final NBTTagCompound tagCompound)
    {
        tagCompound.setDouble("height", mountedYOffset);
    }

    /**
     * Call to tell this entity is being ridden.
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
