package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Module for ignore/acceptance lists of entities.
 */
public interface IEntityListModule
{
    /**
     * Add an entity to the list.
     *
     * @param item the entity to add.
     */
    void addEntity(final ResourceLocation item);

    /**
     * Check if the entity is an allowed item.
     *
     * @param item the entity to check.
     * @return true if so.
     */
    boolean isEntityInList(final ResourceLocation item);

    /**
     * Remove an entity from the list.
     *
     * @param item the item to remove.
     */
    void removeEntity(final ResourceLocation item);

    /**
     * Get a specific entitylist.
     *
     * @return a copy of the list at ID, or an empty list.
     */
    List<ResourceLocation> getList();

    /**
     * Get the string identifier of the list.
     * @return the string.
     */
    String getListIdentifier();

    /**
     * Get the unique id of this module.
     * @return the id.
     */
    String getId();
}
