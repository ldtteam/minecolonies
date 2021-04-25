package com.minecolonies.api.colony;

import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.Nullable;

/**
 * Data to store in a citizen grave
 */
public interface IGraveData
{
    /**
     * get the data NBT of the citizen .
     */
    @Nullable
    CompoundNBT getCitizenDataNBT();

    /**
     * Set data NBT of the citizen .
     */
    void setCitizenDataNBT(@Nullable CompoundNBT citizenDataNBT);

    /**
     * Get the name of the citizen.
     */
    @Nullable
    String getCitizenName();

    /**
     * Set the name of the citizen.
     */
    void setCitizenName(@Nullable String citizenName);

    /**
     * Get the name of the job of the citizen
     */
    @Nullable
    String getCitizenJobName();

    /**
     * Set the name of the job of the citizen
     */
    void setCitizenJobName(@Nullable String citizenJobName);

    /**
     * Read this CitizenData from the compoundNBT
     * @param compoundNBT
     */
    void read(CompoundNBT compoundNBT);

    /**
     * Write this CitizenData to a coumpoundNBT
     * @return
     */
    CompoundNBT write();
}
