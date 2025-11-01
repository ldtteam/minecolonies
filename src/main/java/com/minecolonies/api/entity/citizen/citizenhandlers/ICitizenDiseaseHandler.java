package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.core.datalistener.model.Disease;
import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Citizen disease handler interface.
 */
public interface ICitizenDiseaseHandler
{
    /**
     * To tick the handler.
     */
    void update(final int tickRate);

    /**
     * Check if the citizen is sick and must be healed..
     *
     * @return true if so.
     */
    boolean isSick();

    /**
     * Write the handler to NBT.
     *
     * @param compound the nbt to write it to.
     */
    void write(final CompoundTag compound);

    /**
     * Read the handler from NBT.
     *
     * @param compound the nbt to read it from.
     */
    void read(final CompoundTag compound);

    /**
     * Get the current disease, if any.
     *
     * @return the disease instance.
     */
    @Nullable
    Disease getDisease();

    /**
     * Cure the citizen.
     */
    void cure();

    /**
     * Called when two citizens collide.
     */
    void onCollission(final ICitizenData citizen);

    /**
     * True when the citizen needs to go to a hospital because its hurt
     * @return
     */
    boolean isHurt();

    /**
     * Whether the citizen sleeps at a hospital
     * @return
     */
    boolean sleepsAtHospital();

    /**
     * Sets a flag that the citizen is now at the hospital.
     */
    void setSleepsAtHospital(final boolean isAtHospital);

    /**
     * Set a disease on the citizen.
     *
     * @param disease to set.
     * @return true if they actually became sick.
     */
    boolean setDisease(Disease disease);
}
