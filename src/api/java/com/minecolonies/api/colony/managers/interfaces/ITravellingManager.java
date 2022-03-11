package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Manages the traveling system for a given colony.
 */
public interface ITravellingManager
{
    default boolean isTravelling(final ICitizenData citizenData) {
        return isTravelling(citizenData.getId());
    }

    boolean isTravelling(final int citizenId);

    default Optional<BlockPos> getTravellingTargetFor(final ICitizenData citizenData) {
        return getTravellingTargetFor(citizenData.getId());
    }

    Optional<BlockPos> getTravellingTargetFor(final int citizenId);

    default void startTravellingTo(final ICitizenData citizenData, final BlockPos target, final int travelTimeInTicks) {
        startTravellingTo(citizenData.getId(), target, travelTimeInTicks);
    }

    void startTravellingTo(final int citizenId, final BlockPos target, final int travelTimeInTicks);

    default void finishTravellingFor(final ICitizenData citizenData) {
        finishTravellingFor(citizenData.getId());
    }

    void finishTravellingFor(final int citizenId);

    void recallAllTravellingCitizens();
}
