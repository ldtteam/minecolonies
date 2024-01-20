package com.minecolonies.api.colony.expeditions;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for expedition members.
 */
public interface IExpeditionMember
{
    /**
     * Get the id of the expedition member.
     *
     * @return the civilian id.
     */
    int getId();

    /**
     * Get the name of the expedition member.
     *
     * @return the name of the civilian.
     */
    String getName();

    /**
     * Get whether this expedition member died during the expedition.
     *
     * @return true if so.
     */
    boolean isDead();

    /**
     * Mark this expedition member as dead.
     */
    void died();

    /**
     * Attempt to resolve the civilian data for this expedition member.
     * May return null for multiple reasons.
     *
     * @return the civilian data, or null.
     */
    @Nullable
    ICivilianData resolveCivilianData(final IColony colony);

    /**
     * Write this member to compound data.
     *
     * @param compound the compound tag.
     */
    void write(final CompoundTag compound);
}