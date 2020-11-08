package com.minecolonies.api.colony.buildings.modules.stat;

import java.util.function.BiFunction;

/**
 * Building stat mode enum for the different modes.
 */
public enum BuildingStatMode implements IBuildingStatMode
{
    SUM(IBuildingStat::add),
    FIRST(IBuildingStat::first),
    LAST(IBuildingStat::last);

    /**
     * The mode to calculate the output for to stat values.
     */
    private final BiFunction<IBuildingStat<Number>, IBuildingStat<Number>, IBuildingStat<Number>> mode;

    /**
     * Create a new stat mode.
     * @param mode the processing function.
     */
    BuildingStatMode (final BiFunction<IBuildingStat<Number>, IBuildingStat<Number>, IBuildingStat<Number>> mode)
    {
        this.mode = mode;
    }

    @Override
    public IBuildingStat<Number> process(IBuildingStat<Number> stat1, IBuildingStat<Number> stat2)
    {
        return mode.apply(stat1, stat2);
    }
}
