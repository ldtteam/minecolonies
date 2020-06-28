package com.minecolonies.api.entity.citizen.happiness;

import java.util.function.DoubleSupplier;

/**
 * Static modifier that doesn't change.
 */
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
     * @param supplier the supplier to get the factor.
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
