package com.minecolonies.structures.fake;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Holds a fake entity.
 */
public class FakeEntity extends Entity
{

    /**
     * Create the fake entity.
     *
     * @param worldIn at the position.
     */
    public FakeEntity(final World worldIn)
    {
        super(worldIn);
        this.setPosition(0, 0, 0);
    }

    @Override
    protected void entityInit()
    {
         /*
         * Intentionally left empty.
         */
    }

    @Override
    protected void readEntityFromNBT(@NotNull final NBTTagCompound compound)
    {
         /*
         * Intentionally left empty.
         */
    }

    @Override
    protected void writeEntityToNBT(@NotNull final NBTTagCompound compound)
    {
         /*
         * Intentionally left empty.
         */
    }
}
