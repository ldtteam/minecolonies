package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_VALUE;

/**
 * Static Happiness supplier.
 */
public class StaticHappinessSupplier implements IHappinessSupplierWrapper
{
    /**
     * Static value of supplier.
     */
    private double value;

    /**
     * Create a new static supplier.
     * @param value the static value to supply.
     */
    public StaticHappinessSupplier(final double value)
    {
        this.value = value;
    }

    /**
     * Default constructor for deserialization.
     */
    public StaticHappinessSupplier()
    {
        // Empty on purpose.
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.putDouble(TAG_VALUE, this.value);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.value = nbt.getDouble(TAG_VALUE);
    }

    @Override
    public double getValue(final ICitizenData citizenData)
    {
        return value;
    }

    @Override
    public double getLastCachedValue()
    {
        return value;
    }
}
