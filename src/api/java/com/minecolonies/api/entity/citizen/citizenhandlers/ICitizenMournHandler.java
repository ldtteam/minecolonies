package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;

/**
 * The citizen happiness handler interface.
 */
public interface ICitizenMournHandler
{
    /**
     * Read the handler from NBT.
     *
     * @param compound the compound to read it from.
     */
    void read(CompoundTag compound);

    /**
     * Write the handler to NBT.
     *
     * @param compound the compound to write it to.
     */
    void write(CompoundTag compound);

    /**
     * Add a deceased citizen to the handler.
     * @param name the name of the citizen.
     */
    void addDeceasedCitizen(final String name);

    /**
     * Gets a set with all the recently deceased citizens.
     * @return a set with all the recently deceased citizens.
     */
    Set<String> getDeceasedCitizens();

    /**
     * Remove a deceased citizen from the handler.
     * @param name the name of the citizen.
     */
    void removeDeceasedCitizen(final String name);

    /**
     * Clear the list of deceased citizens.
     */
    void clearDeceasedCitizen();

    /**
     * Check if the citizen should mourn.
     * @return true if so.
     */
    boolean shouldMourn();

    /**
     * Check if the citizen is already mourning.
     * @return true if so.
     */
    boolean isMourning();

    /**
     * Set if the citizen is mourning atm.
     * @param mourn true if so.
     */
    void setMourning(boolean mourn);
}
