package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Wrapper to deal with happiness suppliers.
 */
public interface IHappinessSupplierWrapper extends INBTSerializable<CompoundTag>
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
}
