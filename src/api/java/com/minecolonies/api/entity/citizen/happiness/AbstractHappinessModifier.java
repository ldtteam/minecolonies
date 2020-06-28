package com.minecolonies.api.entity.citizen.happiness;

import net.minecraft.nbt.CompoundNBT;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_VALUE;

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
        compoundNBT.putDouble(TAG_VALUE, getFactor());
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
    public int getDays()
    {
        return 0;
    }

    @Override
    public double getWeight()
    {
        return weight;
    }
}
