package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Wrapper to deal with happiness suppliers.
 */
public interface IHappinessSupplierWrapper
{
    /**
     * Get the matching value.
     * @param citizenData the context.
     * @return the value.
     */
    double getValue(final ICitizenData citizenData);

    /**
     * Get the last cache value in absence of the citizen.
     * @return the last cached value.
     */
    double getLastCachedValue();

    CompoundTag serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt);
}
