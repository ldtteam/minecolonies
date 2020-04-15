package com.minecolonies.coremod.entity.citizen.happiness;

import com.minecolonies.api.util.Tuple;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.DoubleSupplier;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DAY;

/**
 * The time based happiness modifier.
 */
public class TimeBasedHappinessModifier extends StaticHappinessModifier
{
    /**
     * The time based factors.
     */
    private final Tuple<Integer, Double>[] timeBasedFactor;

    /**
     * The number of passed days.
     */
    private int days = 0;

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id     its string id.
     * @param weight its weight.
     */
    public TimeBasedHappinessModifier(final String id, final double weight, final DoubleSupplier supplier, final Tuple<Integer, Double>[] timeBasedFactor)
    {
        super(id, weight, supplier);
        this.timeBasedFactor = timeBasedFactor;
    }

    @Override
    public double getFactor()
    {
        final double baseFactor = super.getFactor();

        double factor = baseFactor;
        if (baseFactor < 1.0)
        {
            for (final Tuple<Integer, Double> tuple : timeBasedFactor)
            {
                if (this.days > tuple.getA())
                {
                    factor = baseFactor * tuple.getB();
                }
            }
        }
        return factor;
    }

    @Override
    public void reset()
    {
        this.days = 0;
    }

    @Override
    public void dayEnd()
    {
        super.dayEnd();
        if (getFactor() < 1)
        {
            days++;
        }
        else
        {
            reset();
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
