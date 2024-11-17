package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.core.BlockPos;

public interface ICitizenSleepHandler
{
    /**
     * Is the citizen a sleep?
     *
     * @return true when a sleep.
     */
    boolean isAsleep();

    /**
     * Attempts a sleep interaction with the citizen and the given bed.
     *
     * @param bedLocation The possible location to sleep.
     * @return if successful.
     */
    boolean trySleep(BlockPos bedLocation);

    /**
     * Called when the citizen wakes up.
     */
    void onWakeUp();

    /**
     * Get the bed location of the citizen.
     *
     * @return the bed location.
     */
    BlockPos getBedLocation();

    /**
     * Whether we should start to go sleeping
     *
     * @return true if should sleep
     */
    boolean shouldGoSleep();
}
