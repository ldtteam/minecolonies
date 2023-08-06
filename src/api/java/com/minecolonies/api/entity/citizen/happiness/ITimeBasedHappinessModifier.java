package com.minecolonies.api.entity.citizen.happiness;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.nbt.CompoundTag;

/**
 * Interface describing possible happiness factors.
 */
public interface ITimeBasedHappinessModifier extends IHappinessModifier
{
    /**
     * Called at the end of each day.
     */
    default void dayEnd(final ICitizenData data) { }

    /**
     * Reset the modifier.
     */
    default void reset() { }

    /**
     * Get the days this is active.
     *
     * @return the days.
     */
    int getDays();
}
