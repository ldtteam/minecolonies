package com.minecolonies.coremod.entity;

import com.minecolonies.coremod.colony.Colony;

/**
 * Interface type for entities belonging to a colony
 */
public interface IColonyRelatedEntity
{
    /**
     * Register the entity with the related colony
     */
    void registerWithColony();

    /**
     * Gets the colony this entity belongs to
     */
    Colony getColony();

    /**
     * Set the colony.
     *
     * @param colony the colony to set.
     */
    void setColony(final Colony colony);
}
