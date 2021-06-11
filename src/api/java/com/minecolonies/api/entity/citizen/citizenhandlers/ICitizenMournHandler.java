package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.nbt.CompoundNBT;

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
    void read(CompoundNBT compound);

    /**
     * Write the handler to NBT.
     *
     * @param compound the compound to write it to.
     */
    void write(CompoundNBT compound);

    /**
     * Add a deceased citizen to the handler.
     * @param name the name of the citizen.
     */
    void addDeceasedCitizen(final String name);

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
