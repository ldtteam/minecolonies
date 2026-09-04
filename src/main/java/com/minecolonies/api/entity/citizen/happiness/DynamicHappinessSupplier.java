package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
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
    private Identifier key;

    /**
     * Last value.
     */
    private double lastValue = 0.0;

    /**
     * Create a new dynamic supplier.
     * @param key the key of the function.
     */
    public DynamicHappinessSupplier(final Identifier key)
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
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString(TAG_ID, key.toString());
        compoundTag.putDouble(TAG_VALUE, lastValue);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt)
    {
        this.key = Identifier.parse(nbt.getStringOr(TAG_ID, ""));
        this.lastValue = nbt.getDoubleOr(TAG_VALUE, 0.0D);
    }

    @Override
    public double getValue(final ICitizenData citizenData)
    {
        HappinessRegistry.HappinessFunctionEntry function = IMinecoloniesAPI.getInstance().getHappinessFunctionRegistry().getValue(key);
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
