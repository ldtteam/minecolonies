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
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    int getStrength();

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    int getEndurance();

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    int getCharisma();

    /**
     * Intelligence getter.
     *
     * @return citizen Intelligence value.
     */
    int getIntelligence();

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    int getDexterity();

    /**
     * Entity experience getter.
     *
     * @return it's experience.
     */
    double getExperience();

    /**
     * Getter for the saturation.
     *
     * @return the saturation.
     */
    double getSaturation();

    /**
     * Health getter.
     *
     * @return citizen Dexterity value
     */
    double getHealth();

    /**
     * Max health getter.
     *
     * @return citizen Dexterity value.
     */
    double getMaxHealth();

    /**
     * Check if the entity is a child
     *
     * @return true if child
     */
    boolean isChild();

    /**
     * Returns the levels of the citizen.
     *
     * @return levels of the citizen.
     */
    int getLevel();

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
