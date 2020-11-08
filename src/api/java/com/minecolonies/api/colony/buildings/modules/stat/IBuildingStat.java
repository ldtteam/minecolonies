package com.minecolonies.api.colony.buildings.modules.stat;

/**
 * A specific building stat.
 */
@FunctionalInterface
public interface IBuildingStat<T extends Number>
{
    /**
     * Process the stat value.
     * @param value2 the input
     * @return the processed result of the input and itself.
     */
    IBuildingStat<T> process(final IBuildingStat<T> value2);
}
