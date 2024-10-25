package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The time based happiness modifier. Over a time the buff/boost gets worse.
 */
public final class TimeBasedHappinessModifier extends AbstractHappinessModifier implements ITimeBasedHappinessModifier
{
    /**
     * A predicate to check whether the current day should roll over or reset.
     */
    private BiPredicate<TimeBasedHappinessModifier, ICitizenData> dayRollOverPredicate;

    /**
     * The time based factors.
     */
    private List<Tuple<Integer, Double>> timeBasedFactor = new ArrayList<>();

    /**
     * The number of passed days.
     */
    private int days = 0;

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id              its string id.
     * @param weight          its weight.
     * @param supplier        the supplier to get the factor.
     * @param timeBasedFactor tuples about the boost/buff factor over time.
     */
    @SafeVarargs
    public TimeBasedHappinessModifier(final String id, final double weight, final IHappinessSupplierWrapper supplier, final Tuple<Integer, Double>... timeBasedFactor)
    {
        this(id, weight, supplier, (modifier, data) -> modifier.getFactor(data) < 1, timeBasedFactor);
    }

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id                   its string id.
     * @param weight               its weight.
     * @param supplier             the supplier to get the factor.
     * @param timeBasedFactor      tuples about the boost/buff factor over time.
     * @param dayRollOverPredicate a predicate to check whether the current day should roll over or reset.
     */
    @SafeVarargs
    public TimeBasedHappinessModifier(
      final String id,
      final double weight,
      final IHappinessSupplierWrapper supplier,
      final BiPredicate<TimeBasedHappinessModifier, ICitizenData> dayRollOverPredicate,
      final Tuple<Integer, Double>... timeBasedFactor)
    {
        super(id, weight, supplier);
        this.dayRollOverPredicate = dayRollOverPredicate;
        this.timeBasedFactor = List.of(timeBasedFactor);
    }

    /**
     * Create an instance of the happiness modifier.
     */
    public TimeBasedHappinessModifier()
    {
        super();
    }

    @Override
    public double getFactor(final ICitizenData citizenData)
    {
        final double baseFactor = super.getFactor(citizenData);

        double factor = baseFactor;
        if (baseFactor < 1.0)
        {
            for (final Tuple<Integer, Double> tuple : timeBasedFactor)
            {
                if (this.days >= tuple.getA())
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
    public int getDays()
    {
        return days;
    }

    @Override
    public void dayEnd(final ICitizenData data)
    {
        if (dayRollOverPredicate.test(this, data))
        {
            days++;
        }
        else
        {
            reset();
        }
    }

    @Override
    public void read(final CompoundTag compoundNBT, final boolean persist)
    {
        super.read(compoundNBT, persist);
        this.days = compoundNBT.getInt(TAG_DAY);
        if (!persist)
        {
            final ListTag listTag = compoundNBT.getList(TAG_LIST, Constants.TAG_COMPOUND);
            final List<Tuple<Integer, Double>> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++)
            {
                final CompoundTag entryTag = listTag.getCompound(i);
                list.add(new Tuple<>(entryTag.getInt(TAG_DAY), entryTag.getDouble(TAG_VALUE)));
            }
            this.timeBasedFactor = list;
        }
    }

    @Override
    public void write(final CompoundTag compoundNBT, final boolean persist)
    {
        super.write(compoundNBT, persist);
        compoundNBT.putString(NbtTagConstants.TAG_MODIFIER_TYPE, HappinessRegistry.TIME_PERIOD_MODIFIER.toString());
        compoundNBT.putInt(TAG_DAY, days);
        if (!persist)
        {
            final ListTag listTag = new ListTag();
            for (final Tuple<Integer, Double> entry : timeBasedFactor)
            {
                final CompoundTag listEntry = new CompoundTag();
                listEntry.putInt(TAG_DAY, entry.getA());
                listEntry.putDouble(TAG_VALUE, entry.getB());
                listTag.add(listEntry);
            }
            compoundNBT.put(TAG_LIST, listTag);
        }
    }
}
