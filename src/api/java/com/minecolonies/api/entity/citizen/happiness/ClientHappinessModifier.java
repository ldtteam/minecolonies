package com.minecolonies.api.entity.citizen.happiness;

import net.minecraft.nbt.CompoundNBT;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_VALUE;

/**
 * Client happiness modifier class.
 */
public class ClientHappinessModifier extends AbstractHappinessModifier
{
    /**
     * The supplier to get the happiness factor.
     */
    private double value;

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id     its string id.
     * @param weight its weight.
     */
    public ClientHappinessModifier(final String id, final double weight)
    {
        super(id, weight);
    }

    @Override
    public void read(final CompoundNBT compoundNBT)
    {
        this.value = compoundNBT.getDouble(TAG_VALUE);
    }

    @Override
    public double getFactor()
    {
        return value;
    }
}
