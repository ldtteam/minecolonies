package com.minecolonies.api.colony;

import com.minecolonies.api.inventory.InventoryCitizen;

/**
 * Higher level Citizen data (Englobes server and client side variants).
 */
public interface ICitizen
{
    /**
     * Returns the id of the citizen.
     *
     * @return id of the citizen.
     */
    int getId();

    /**
     * Returns the name of the citizen.
     *
     * @return name of the citizen.
     */
    String getName();

    /**
     * Returns true if citizen is female, false for male.
     *
     * @return true for female, false for male.
     */
    boolean isFemale();

    /**
     * Getter for the saturation.
     *
     * @return the saturation.
     */
    double getSaturation();

    /**
     * Check if the entity is a child
     *
     * @return true if child
     */
    boolean isChild();

    /**
     * Get the inventory of the citizen.
     * @return the inventory of the citizen.
     */
    InventoryCitizen getInventory();

    /**
     * Check if the citizen is paused.
     */
    void setPaused(boolean p);

    /**
     * Check if the citizen is paused.
     *
     * @return true for paused, false for working.
     */
    boolean isPaused();
}
