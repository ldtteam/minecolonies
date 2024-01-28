package com.minecolonies.api.colony.buildings.modules.stat;

import org.jetbrains.annotations.NotNull;

/**
 * A specific stat.
 */
@FunctionalInterface
public interface IStat<N extends Number>
{
    /**
     * Apply the input value to the existing value.
     * @param input the input value.
     * @return the calculated output.
     */
    N apply(@NotNull final N input);
}
