package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Abstract happiness modifier implementation.
 */
public abstract class AbstractHappinessModifier implements IHappinessModifier
{
    /**
     * The supplier to get the happiness factor.
     */
    private IHappinessSupplierWrapper supplier;

    /**
     * The id of the modifier.
     */
    public String id;

    /**
     * The weight of the modifier.
     */
    private double weight;

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id     its string id.
     * @param weight its weight.
     */
    public AbstractHappinessModifier(final String id, final double weight, final IHappinessSupplierWrapper supplier)
    {
        this.id = id;
        this.weight = weight;
        this.supplier = supplier;
    }

    @Override
    public double getFactor(@Nullable final ICitizenData citizenData)
    {
        return citizenData == null ? supplier.getLastCachedValue() : supplier.getValue(citizenData);
    }

    /**
     * Create an empty instance of the abstract happiness modifier.
     */
    public AbstractHappinessModifier()
    {
        super();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void read(final CompoundTag compoundNBT, final boolean persist)
    {
        this.id = compoundNBT.getString(TAG_ID);
        this.weight = compoundNBT.getDouble(TAG_WEIGHT);
        final CompoundTag supplierCompound = compoundNBT.getCompound(TAG_SUPPLIER);
        if (supplierCompound.contains(TAG_ID))
        {
            supplier = new DynamicHappinessSupplier();
        }
        else
        {
            supplier = new StaticHappinessSupplier();
        }
        supplier.deserializeNBT(supplierCompound);
    }

    @Override
    public void write(final CompoundTag compoundNBT, final boolean persist)
    {
        compoundNBT.putString(TAG_ID, this.id);
        compoundNBT.putDouble(TAG_WEIGHT, this.weight);
        compoundNBT.put(TAG_SUPPLIER, this.supplier.serializeNBT());
    }

    @Override
    public double getWeight()
    {
        return weight;
    }
}
