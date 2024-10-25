package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundTag;

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
    public void write(final CompoundTag compoundNBT, final boolean persist)
    {
        super.write(compoundNBT, persist);
        compoundNBT.putString(NbtTagConstants.TAG_MODIFIER_TYPE, HappinessRegistry.STATIC_MODIFIER.toString());
    }
}
