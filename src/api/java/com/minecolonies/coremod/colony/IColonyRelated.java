package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColony;

/**
 * Interface type for entities belonging to a colony
 */
public interface IColonyRelated
{
    /**
     * Register the entity with the related colony
     */
    void registerWithColony();

    /**
     * Gets the colony this entity belongs to
     */
    IColony getColony();

    /**
     * Set the colony.
     *
     * @param colony the colony to set.
     */
    void setColony(final IColony colony);
}
