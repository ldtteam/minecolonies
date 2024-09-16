package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * Static modifier that doesn't change.
 */
public final class StaticHappinessModifier extends AbstractHappinessModifier
{
    /**
     * Create an instance of the happiness modifier.
     *
     * @param id       its string id.
     * @param weight   its weight.
     * @param supplier the supplier to get the factor.
     */
    public StaticHappinessModifier(final String id, final double weight, final IHappinessSupplierWrapper supplier)
    {
        super(id, weight, supplier);
    }

    /**
     * Create an instance of the static happiness modifier.
     */
    public StaticHappinessModifier()
    {
        super();
    }

    @Override
    public void write(@NotNull final HolderLookup.Provider provider, final CompoundTag compoundNBT, final boolean persist)
    {
        super.write(provider, compoundNBT, persist);
        compoundNBT.putString(NbtTagConstants.TAG_MODIFIER_TYPE, HappinessRegistry.STATIC_MODIFIER.toString());
    }
}
