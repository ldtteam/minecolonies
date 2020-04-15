package com.minecolonies.coremod.entity.citizen.happiness;

import java.util.function.DoubleSupplier;

public class StaticHappinessModifier extends AbstractHappinessModifier
{
    /**
     * The supplier to get the happiness factor.
     */
    private final DoubleSupplier supplier;

    /**
     * Create an instance of the happiness modifier.
     *
     * @param id     its string id.
     * @param weight its weight.
     */
    public StaticHappinessModifier(final String id, final double weight, final DoubleSupplier supplier)
    {
        super(id, weight);
        this.supplier = supplier;
    }

    @Override
    public double getFactor()
    {
        return supplier.getAsDouble();
    }
}
