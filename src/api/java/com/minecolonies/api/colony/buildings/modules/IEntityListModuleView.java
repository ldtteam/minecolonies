package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.resources.ResourceLocation;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public interface IEntityListModuleView extends IBuildingModuleView
{
    /**
     * Add entity to the view and notify the server side.
     *
     * @param entity the entity to add.
     */
    void addEntity(final ResourceLocation entity);

    /**
     * Check if an entity is in the list of allowed entities.
     *
     * @param entity the entity to check.
     * @return true if so.
     */
    boolean isAllowedEntity(final ResourceLocation entity);
    /**
     * Get the size of allowed items.
     *
     * @return the size.
     */
    int getSize();

    /**
     * Remove an entity from the view and notify the server side.
     *
     * @param entity the entity to remove.
     */
    void removeEntity(final ResourceLocation entity);

    /**
     * Get the unique id of this group (used to sync with server side).
     * @return the id.
     */
    String getId();

    /**
     * Check if the list is enabling or disabling.
     * @return true if enabling.
     */
    boolean isInverted();

    /**
     * Clear the list of items
     */
    void clearEntities();
}
