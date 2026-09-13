package com.minecolonies.api.eventbus.events.colony.citizens;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyModEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Event fired after an undertaker successfully buries a citizen.
 */
public final class CitizenBuriedModEvent extends AbstractColonyModEvent
{
    private final BlockPos gravePosition;

    private final CompoundTag savedCitizenNbt;

    private final String citizenName;

    private final String citizenJobName;

    private final ICitizen undertaker;

    private final int burialDay;

    /**
     * Construct an event for a successfully buried citizen.
     *
     * @param colony          the colony in which the citizen was buried.
     * @param gravePosition   the position of the citizen's grave.
     * @param savedCitizenNbt the saved citizen data that can be used to resurrect the citizen.
     * @param citizenName     the buried citizen's name.
     * @param citizenJobName  the buried citizen's job name, or {@code null} if unemployed.
     * @param undertaker      the undertaker who buried the citizen.
     */
    public CitizenBuriedModEvent(
      final IColony colony,
      final BlockPos gravePosition,
      final CompoundTag savedCitizenNbt,
      final String citizenName,
      final String citizenJobName,
      final ICitizen undertaker)
    {
        super(colony);
        this.gravePosition = gravePosition.immutable();
        this.savedCitizenNbt = savedCitizenNbt.copy();
        this.citizenName = citizenName;
        this.citizenJobName = citizenJobName;
        this.undertaker = undertaker;
        this.burialDay = colony.getDay();
    }

    /**
     * Get the position of the citizen's grave.
     *
     * @return the immutable grave position.
     */
    public BlockPos getGravePosition()
    {
        return gravePosition;
    }

    /**
     * Get a copy of the saved citizen data suitable for resurrection.
     *
     * @return a defensive copy of the saved citizen NBT.
     */
    public CompoundTag getSavedCitizenNbt()
    {
        return savedCitizenNbt.copy();
    }

    /**
     * Get the undertaker who buried the citizen.
     *
     * @return the undertaker.
     */
    public ICitizen getUndertaker()
    {
        return undertaker;
    }

    /**
     * Get the name of the buried citizen.
     *
     * @return the citizen name.
     */
    public String getCitizenName()
    {
        return citizenName;
    }

    /**
     * Get the last job name of the buried citizen.
     *
     * @return the translated job name, or {@code null} if the citizen was unemployed.
     */
    public String getCitizenJobName()
    {
        return citizenJobName;
    }

    /**
     * Get the colony day on which the burial occurred.
     *
     * @return the burial day captured when the event was created.
     */
    public int getBurialDay()
    {
        return burialDay;
    }
}
