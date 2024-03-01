package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for the colony expedition manager. From here all outgoing expeditions to external places are managed.
 */
public interface IColonyExpeditionManager
{
    /**
     * Get the expedition with the given id.
     *
     * @param id the expedition id.
     * @return the expedition instance or null.
     */
    @Nullable
    ColonyExpedition getExpedition(final int id);

    /**
     * Register a new expedition to the manager.
     *
     * @param expedition the expedition instance.
     * @return the new expedition instance containing a new id.
     */
    @Nullable
    ColonyExpedition addExpedition(final ColonyExpedition expedition);

    /**
     * Mark as expedition as finished.
     *
     * @param id the expedition id.
     */
    void finishExpedition(final int id);

    /**
     * Get whether another expedition may be started (determines if another visitor may spawn).
     *
     * @return true if so.
     */
    boolean canStartNewExpedition();

    /**
     * Check that determines if expeditions to a given dimension may be sent or not.
     *
     * @param dimension the target dimension.
     * @return whether the target dimension is allowed or not.
     */
    boolean canGoToDimension(final ResourceKey<Level> dimension);

    /**
     * Writes the expedition manager to compound data.
     *
     * @param compound the compound data to read from.
     */
    void read(final CompoundTag compound);

    /**
     * Writes the expedition manager to compound data.
     *
     * @param compound the compound data to write to.
     */
    void write(final CompoundTag compound);
}