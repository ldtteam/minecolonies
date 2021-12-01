package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.crafting.ItemStorage;

import java.util.List;

/**
 * Module for ignore/acceptance lists of items.
 */
public interface IItemListModule
{
    /**
     * Add an item to the list.
     *
     * @param item the item to add.
     */
    void addItem(final ItemStorage item);

    /**
     * Check if the item is an allowed item.
     *
     * @param item the item to check.
     * @return true if so.
     */
    boolean isItemInList(final ItemStorage item);

    /**
     * Remove an item from the list.
     *
     * @param item the item to remove.
     */
    void removeItem(final ItemStorage item);

    /**
     * Get a specific itemlist.
     *
     * @return a copy of the list at ID, or an empty list.
     */
    List<ItemStorage> getList();

    /**
     * Get the string identifier of the list.
     * @return the string.
     */
    String getListIdentifier();

    /**
     * Clear all in the list.
     */
    void clearItems();

    /**
     * Reset to defaults (usually but not necessarily the same as {@link #clearItems()}).
     */
    void resetToDefaults();

    /**
     * Get the unique id of this module.
     * @return the id.
     */
    String getId();
}
