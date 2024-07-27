package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_VALUE;

/**
 * Dynamic Happiness supplier.
 */
public class DynamicHappinessSupplier implements IHappinessSupplierWrapper
{
    /**
     * Entry key.
     */
    private ResourceLocation key;

    /**
     * Last value.
     */
    private double lastValue = 0.0;

    /**
     * Create a new dynamic supplier.
     * @param key the key of the function.
     */
    public DynamicHappinessSupplier(final ResourceLocation key)
    {
        this.key = key;
    }

    /**
     * Default constructor for deserialization.
     */
    public DynamicHappinessSupplier()
    {
        // Empty on purpose.
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(TAG_ID, key.toString());
        compoundTag.putDouble(TAG_VALUE, lastValue);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.key = new ResourceLocation(nbt.getString(TAG_ID));
        this.lastValue = nbt.getDouble(TAG_VALUE);
    }

    @Override
    public double getValue(final ICitizenData citizenData)
    {
        HappinessRegistry.HappinessFunctionEntry function = IMinecoloniesAPI.getInstance().getHappinessFunctionRegistry().get(key);
        if (function == null)
        {
            return lastValue;
        }
        lastValue = function.getDoubleSupplier().apply(citizenData);
        return lastValue;
    }

    @Override
    public double getLastCachedValue()
    {
        return lastValue;
    }
}
