package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Manages the traveling system for a given colony.
 */
public interface ITravellingManager
{
    /**
     * Indicates whether the given citizen is travelling
     *
     * @param citizenData The citizen data to check for.
     * @return True when travelling, false when not.
     */
    default boolean isTravelling(final ICitizenData citizenData)
    {
        return isTravelling(citizenData.getId());
    }

    /**
     * Indicates whether the given citizen is travelling.
     *
     * @param citizenDataView The citizen data view to check for.
     * @return True when travelling, false when not.
     */
    default boolean isTravelling(final ICitizenDataView citizenDataView)
    {
        return isTravelling(citizenDataView.getId());
    }

    /**
     * Indicates whether the citizen with the given id is travelling or not.
     *
     * @param citizenId The citizen id to check for.
     * @return True when travelling, false when not.
     */
    boolean isTravelling(final int citizenId);

    /**
     * Gets the travelling target, when travelling, for the given citizen.
     *
     * @param citizenData The citizen to get the target for.
     * @return An optional with the travelling target. The optional is empty when the citizen is not travelling.
     */
    default Optional<BlockPos> getTravellingTargetFor(final ICitizenData citizenData)
    {
        return getTravellingTargetFor(citizenData.getId());
    }

    /**
     * Gets the travelling target, when travelling, for the citizen with the given id.
     *
     * @param citizenId The id of the citizen to get the target for.
     * @return An optional with the travelling target. The optional is empty when the citizen is not travelling.
     */
    Optional<BlockPos> getTravellingTargetFor(final int citizenId);

    /**
     * Triggers the travelling logic for the given citizen.
     * <p>
     * Does not despawn the entity. Discard it after calling this method.
     * </p>
     *
     * @param citizenData       The data of the citizen.
     * @param target            The target to which the citizen is travelling, aka his respawn point.
     * @param travelTimeInTicks The time in ticks which needs to have passed for the travelling to complete.
     */
    default void startTravellingTo(final ICitizenData citizenData, final BlockPos target, final int travelTimeInTicks)
    {
        startTravellingTo(citizenData.getId(), target, travelTimeInTicks);
    }

    /**
     * Triggers the travelling logic for the citizen with the given id.
     * <p>
     *     Does not despawn the entity. Discard it after calling this method.
     * </p>
     *
     * @param citizenId The id of the citizen.
     * @param target The target to which the citizen is travelling, aka his respawn point.
     * @param travelTimeInTicks The time in ticks which needs to have passed for the travelling to complete.
     */
    void startTravellingTo(final int citizenId, final BlockPos target, final int travelTimeInTicks);

    /**
     * Triggers the cleanup of the travelling data when the citizen finishes travelling, either by design, or through an abort.
     * <p>
     *     Does not spawn the entity. Trigger an entity update for the citizen to properly spawn it.
     * </p>
     *
     * @param citizenData The citizen to stop the travelling for.
     */
    default void finishTravellingFor(final ICitizenData citizenData)
    {
        finishTravellingFor(citizenData.getId());
    }

    /**
     * Triggers the cleanup of the travelling data when the citizen finishes travelling, either by design, or through an abort.
     * <p>
     *     Does not spawn the entity. Trigger an entity update for the citizen to properly spawn it.
     * </p>
     *
     * @param citizenId The id of the citizen to stop the travelling for.
     */
    void finishTravellingFor(final int citizenId);

    /**
     * Triggers the recall of all travelling citizens regards of state of completion.
     */
    void recallAllTravellingCitizens();
}
