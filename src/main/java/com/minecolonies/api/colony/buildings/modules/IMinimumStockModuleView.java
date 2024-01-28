package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Tuple;

import java.util.List;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public interface IMinimumStockModuleView extends IBuildingModuleView
{
    /**
     * The minimum stock.
     *
     * @return the stock.
     */
    List<Tuple<ItemStorage, Integer>> getStock();

    /**
     * Check if the warehouse has reached the limit.
     *
     * @return true if so.
     */
    boolean hasReachedLimit();
}
