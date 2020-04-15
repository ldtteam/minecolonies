package com.minecolonies.coremod.entity.citizen.happiness;

import net.minecraft.nbt.CompoundNBT;

import java.util.function.DoubleSupplier;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DAY;

/**
 * The time based happiness modifier.
 */
public class ExpirationBasedHappinessModifier extends StaticHappinessModifier
{
    /**
     * The number of passed days.
     */
    private int days = 0;

    /**
     * Period of time this modifier applies.
     */
    private final int period;

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id     its string id.
     * @param weight its weight.
     */
    public ExpirationBasedHappinessModifier(final String id, final double weight, final DoubleSupplier supplier, final int period)
    {
        super(id, weight, supplier);
        this.period = period;
    }

    @Override
    public double getFactor()
    {
        if (days > 0)
        {
            return super.getFactor();
        }
        return 1;
    }

    @Override
    public void reset()
    {
        this.days = period;
    }

    @Override
    public void dayEnd()
    {
        super.dayEnd();
        if (days > 0)
        {
            days--;
        }
    }

    @Override
    public void read(final CompoundNBT compoundNBT)
    {
        super.read(compoundNBT);
        this.days = compoundNBT.getInt(TAG_DAY);
    }

    @Override
    public void write(final CompoundNBT compoundNBT)
    {
        super.write(compoundNBT);
        compoundNBT.putInt(TAG_DAY, days);
    }
}
