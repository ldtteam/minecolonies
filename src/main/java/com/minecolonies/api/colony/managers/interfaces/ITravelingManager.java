package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

/**
 * Manages the traveling system for a given colony.
 */
public interface ITravelingManager extends INBTSerializable<CompoundTag>
{
    /**
     * Get whether a citizen is currently travelling.
     *
     * @param citizenData the citizen data.
     * @return true if so.
     */
    default boolean isTravelling(final ICitizenData citizenData)
    {
        return isTravelling(citizenData.getId());
    }

    /**
     * Get whether a citizen is currently travelling.
     *
     * @param citizenId the id of the citizen.
     * @return true if so.
     */
    boolean isTravelling(final int citizenId);

    /**
     * Get the target the citizen is travelling to.
     *
     * @param citizenData the citizen data.
     * @return an optional position.
     */
    default Optional<BlockPos> getTravellingTargetFor(final ICitizenData citizenData)
    {
        return getTravellingTargetFor(citizenData.getId());
    }

    /**
     * Get the target the citizen is travelling to.
     *
     * @param citizenId the id of the citizen.
     * @return an optional position.
     */
    Optional<BlockPos> getTravellingTargetFor(final int citizenId);

    /**
     * Tells the given citizen to start travelling to a given location.
     *
     * @param citizenData       the citizen data.
     * @param target            the target position.
     * @param travelTimeInTicks the time it will take to travel to the given location.
     * @param canRecall         whether the given citizen is allowed to be recalled or not.
     */
    default void startTravellingTo(final ICitizenData citizenData, final BlockPos target, final int travelTimeInTicks, final boolean canRecall)
    {
        startTravellingTo(citizenData.getId(), target, travelTimeInTicks, canRecall);
    }

    /**
     * Start travelling to a given location for a citizen.
     *
     * @param citizenId         the id of the citizen.
     * @param target            the target position.
     * @param travelTimeInTicks the time it will take to travel to the given location.
     * @param canRecall         whether the given citizen is allowed to be recalled or not.
     */
    void startTravellingTo(final int citizenId, final BlockPos target, final int travelTimeInTicks, final boolean canRecall);

    /**
     * Finish travelling for a citizen.
     *
     * @param citizenData the citizen data.
     */
    default void finishTravellingFor(final ICitizenData citizenData)
    {
        finishTravellingFor(citizenData.getId());
    }

    /**
     * Finish travelling for a citizen.
     *
     * @param citizenId the id of the citizen.
     */
    void finishTravellingFor(final int citizenId);

    /**
     * Finishes travelling for all citizens currently away travelling.
     */
    void recallAllTravellingCitizens();

    /**
     * Whether the expedition manager class is dirty and the client needs to be updated.
     *
     * @return true if so.
     */
    boolean isDirty();

    /**
     * Update the dirty flag of the expedition manager.
     *
     * @param dirty the new dirty state.
     */
    void setDirty(boolean dirty);
}