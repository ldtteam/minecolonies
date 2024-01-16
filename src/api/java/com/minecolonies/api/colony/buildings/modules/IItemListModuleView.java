package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;

import java.util.Set;
import java.util.function.Function;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public interface IItemListModuleView extends IBuildingModuleView
{
    /**
     * Add item to the view and notify the server side.
     *
     * @param item the item to add.
     */
    void addItem(final ItemStorage item);

    /**
     * Check if an item is in the list of allowed items.
     *
     * @param item the item to check.
     * @return true if so.
     */
    boolean isAllowedItem(final ItemStorage item);
    /**
     * Get the size of allowed items.
     *
     * @return the size.
     */
    int getSize();

    /**
     * Remove an item from the view and notify the server side.
     *
     * @param item the item to remove.
     */
    void removeItem(final ItemStorage item);

    /**
     * Get the unique id of this group (used to sync with server side).
     * @return the id.
     */
    String getId();

    /**
     * Get the supplier of the list of all items to display.
     * @return the list.
     */
    Function<IBuildingView, Set<ItemStorage>> getAllItems();

    /**
     * Check if the list is enabling or disabling.
     * @return true if enabling.
     */
    boolean isInverted();

    /**
     * Clear the list of items
     */
    void clearItems();
}
