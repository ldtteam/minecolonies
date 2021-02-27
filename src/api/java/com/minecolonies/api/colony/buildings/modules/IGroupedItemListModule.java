package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.crafting.ItemStorage;

import java.util.List;
import java.util.Map;

/**
 * Module for
 */
public interface IGroupedItemListModule
{
    /**
     * Add an item to the list.
     *
     * @param id   the string id of the item type.
     * @param item the item to add.
     */
    void addItem(final String id, final ItemStorage item);

    /**
     * Check if the item is an allowed item.
     *
     * @param item the item to check.
     * @param id   the string id of the item type.
     * @return true if so.
     */
    boolean isItemInList(final String id, final ItemStorage item);

    /**
     * Remove an item from the list.
     *
     * @param id   the string id of the item type.
     * @param item the item to remove.
     */
    void removeItem(final String id, final ItemStorage item);

    /**
     * Get a specific itemlist.
     *
     * @return a copy of the list at ID, or an empty list.
     */
    List<ItemStorage> getList(final String id);
}
