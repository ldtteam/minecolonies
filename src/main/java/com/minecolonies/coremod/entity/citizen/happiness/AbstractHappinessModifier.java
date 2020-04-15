package com.minecolonies.coremod.entity.citizen.happiness;

import net.minecraft.nbt.CompoundNBT;

/**
 * Abstract happiness modifier implementation.
 */
public abstract class AbstractHappinessModifier implements IHappinessModifier
{
    /**
     * The id of the modifier.
     */
    public final String id;

    /**
     * The weight of the modifier.
     */
    private final double weight;

    /**
     * Create an instance of the happiness modifier.
     * @param id its string id.
     * @param weight its weight.
     */
    public AbstractHappinessModifier(final String id, final double weight)
    {
        this.id = id;
        this.weight = weight;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void read(final CompoundNBT compoundNBT)
    {

    }

    @Override
    public void write(final CompoundNBT compoundNBT)
    {

    }

    @Override
    public void dayEnd()
    {

    }

    @Override
    public void reset()
    {

    }

    @Override
    public void triggerInteractions()
    {

    }

    @Override
    public double getWeight()
    {
        return weight;
    }
}
