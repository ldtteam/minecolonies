package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.nbt.CompoundNBT;

/**
 * Citizen disease handler interface.
 */
public interface ICitizenDiseaseHandler
{
    /**
     * To tick the handler.
     */
    void tick();

    /**
     * Check if the citizen is sick and must be healed..
     * @return true if so.
     */
    boolean isSick();

    /**
     * Write the handler to NBT.
     * @param compound the nbt to write it to.
     */
    void write(final CompoundNBT compound);

    /**
     * Read the handler from NBT.
     * @param compound the nbt to read it from.
     */
    void read(final CompoundNBT compound);

    /**
     * get the disease identifier.
     */
    String getDisease();

    /**
     * Cure the citizen.
     */
    void cure();
}
