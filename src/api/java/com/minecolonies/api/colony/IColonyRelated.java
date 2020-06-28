package com.minecolonies.api.colony;

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
     * @return the colony.
     */
    IColony getColony();

    /**
     * Set the colony.
     *
     * @param colony the colony to set.
     */
    void setColony(final IColony colony);
}
