package com.minecolonies.api.colony.workorders;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.entity.ai.workers.util.BuildingProgressStage;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IBuilderWorkOrder extends IServerWorkOrder
{
    /**
     * Get the amount of resources this work order still requires.
     *
     * @return the amount of resources.
     */
    int getAmountOfResources();

    /**
     * Set the amount of resources this work order still requires.
     *
     * @param newQuantity the new amount of resources.
     */
    void setAmountOfResources(int newQuantity);

    /**
     * Get the iterator type (method of construction) of the work order.
     *
     * @return the iterator type.
     */
    String getIteratorType();

    /**
     * Set the iterator type (method of construction) of the work order.
     *
     * @param iteratorType the new iterator type.
     */
    void setIteratorType(String iteratorType);

    /**
     * Whether the area of the work order has been cleared out or not.
     *
     * @return true if the area is cleared.
     */
    boolean isCleared();

    /**
     * Set whether the area of the work order has been cleared out or not.
     *
     * @param cleared the new cleared state.
     */
    void setCleared(boolean cleared);

    /**
     * Set whether the building has been cleared.
     *
     * @param requested true if the building has been cleared.
     */
    void setRequested(boolean requested);

    /**
     * Set whether the resources for this work order have been requested.
     *
     * @return true when the resources are requested.
     */
    boolean isRequested();

    /**
     * Executed when a work order is completed.
     * <p>
     * Override this when something need to be done when the work order is completed
     *
     * @param colony  in which the work order exist
     * @param citizen citizen that completed the work order
     */
    void onCompleted(IColony colony, ICitizenData citizen);

    /**
     * Executed when a work order is removed.
     * <p>
     * Override this when something need to be done when the work order is removed
     *
     * @param colony in which the work order exist
     */
    void onRemoved(IColony colony);

    /**
     * Check if this workOrder can be resolved by an existing builder.
     *
     * @param colony the colony to check in.
     * @param level  the new level of the building.
     * @return true if so.
     */
    boolean canBeResolved(IColony colony, int level);

    /**
     * Check if this workOrder can be resolved by an existing builder by distance.
     *
     * @param colony the colony to check in.
     * @param level  the new level of the building.
     * @return true if so.
     */
    boolean tooFarFromAnyBuilder(IColony colony, int level);

    /**
     * Checks if a builder may accept this workOrder while ignoring the distance to the builder.
     *
     * @param builderLocation position of the builders own hut.
     * @param builderLevel    level of the builders hut.
     * @return true if so.
     */
    public boolean canBuildIgnoringDistance(@NotNull IBuilding building, @NotNull final BlockPos builderLocation, final int builderLevel);

    /**
     * Sets the building stage of the workorder
     *
     * @param stage
     */
    void setStage(BuildingProgressStage stage);
}
