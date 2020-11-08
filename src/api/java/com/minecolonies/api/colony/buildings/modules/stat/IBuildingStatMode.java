package com.minecolonies.api.colony.buildings.modules.stat;

/**
 * Interface defining a building stat mode.
 */
public interface IBuildingStatMode
{
    /**
     * Process a stat mode with two values.
     * @param stat1 first value.
     * @param stat2 second value.
     * @return the result value.
     */
    IBuildingStat<Number> process(IBuildingStat<Number> stat1, IBuildingStat<Number> stat2);
}
