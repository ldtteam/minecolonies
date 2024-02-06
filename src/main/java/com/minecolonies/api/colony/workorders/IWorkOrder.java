package com.minecolonies.api.colony.workorders;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;

public interface IWorkOrder
{
    /**
     * Get the ID of the work order.
     *
     * @return ID of the work order
     */
    int getID();

    /**
     * Set the ID of the work order.
     *
     * @param id the new ID for the work order
     */
    void setID(int id);

    /**
     * Getter for the priority.
     *
     * @return the priority of the work order.
     */
    int getPriority();

    /**
     * Setter for the priority.
     *
     * @param priority the new priority.
     */
    void setPriority(int priority);

    /**
     * Get the structure this work order should be using, if any.
     *
     * @return the schematic path.
     */
    String getStructurePath();

    /**
     * Get the structure this work order should be using, if any.
     *
     * @return the pack name.
     */
    String getStructurePack();

    /**
     * Get a blueprint future.
     * @return the blueprint future (might contain null).
     */
    Future<Blueprint> getBlueprintFuture();

    /**
     * Get the current level of the structure of the work order.
     *
     * @return the current level.
     */
    int getCurrentLevel();

    /**
     * Get the target level of the structure of the work order.
     *
     * @return the target level.
     */
    int getTargetLevel();

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
     * Set whether the resources for this work order have been requested.
     *
     * @return true when the resources are requested.
     */
    boolean isRequested();

    /**
     * Set whether the building has been cleared.
     *
     * @param requested true if the building has been cleared.
     */
    void setRequested(boolean requested);

    /**
     * Checks if the workOrder has changed.
     *
     * @return true if so.
     */
    boolean isDirty();

    /**
     * Resets the changed variable.
     */
    void resetChange();

    /**
     * The name of the work order.
     *
     * @return the work order name.
     */
    String getTranslationKey();

    /**
     * The type of the work order.
     *
     * @return the work order type.
     */
    WorkOrderType getWorkOrderType();

    /**
     * Get the current location of the building
     *
     * @return the location
     */
    BlockPos getLocation();

    /**
     * Get the current rotation and mirror of the building
     *
     * @return the location
     */
    RotationMirror getRotationMirror();

    /**
     * Is the Work Order claimed?
     *
     * @return true if the Work Order has been claimed
     */
    boolean isClaimed();

    /**
     * Is the Work Order claimed by the given citizen?
     *
     * @param citizen The citizen to check
     * @return true if the Work Order is claimed by this Citizen
     */
    boolean isClaimedBy(@NotNull ICitizenData citizen);

    /**
     * Get the ID of the Citizen that the Work Order is claimed by.
     *
     * @return ID of citizen the Work Order has been claimed by, or null
     */
    BlockPos getClaimedBy();

    /**
     * Set the Work Order as claimed by the given Citizen.
     *
     * @param citizen {@link ICitizenData}
     */
    void setClaimedBy(@Nullable ICitizenData citizen);

    /**
     * Set the Work order as claimed by a given building.
     *
     * @param builder the building position.
     */
    void setClaimedBy(BlockPos builder);

    /**
     * Clear the Claimed By status of the Work Order.
     */
    void clearClaimedBy();

    /**
     * Get the name of the work order, provides the custom name or the work order name when no custom name is given
     *
     * @return the display name for the work order
     */
    Component getDisplayName();

    /**
     * Is this WorkOrder still valid? If not, it will be deleted.
     * <p>
     * Suppressing Sonar Rule squid:S1172 This rule does " Unused method parameters should be removed" But in this case extending class may need to use the colony parameter
     *
     * @param colony The colony that owns the Work Order
     * @return True if the WorkOrder is still valid, or False if it should be deleted
     */
    @SuppressWarnings(UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED)
    boolean isValid(IColony colony);

    /**
     * Read the WorkOrder data from the CompoundTag.
     *
     * @param compound NBT Tag compound
     * @param manager  the workManager calling this method.
     */
    void read(@NotNull CompoundTag compound, IWorkManager manager);

    /**
     * Save the Work Order to an CompoundTag.
     *
     * @param compound NBT tag compount
     */
    void write(@NotNull CompoundTag compound);

    /**
     * Writes the workOrders data to a byte buf for transition.
     *
     * @param buf Buffer to write to
     */
    void serializeViewNetworkData(@NotNull FriendlyByteBuf buf);

    /**
     * Executed when a work order is added.
     * <p>
     * Override this when something need to be done when the work order is added
     *
     * @param colony         in which the work order exist
     * @param readingFromNbt if being read from NBT.
     */
    void onAdded(IColony colony, boolean readingFromNbt);

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

    boolean canBuild(@NotNull ICitizenData citizen);

    /**
     * Whether this work order can be made by a builder.
     *
     * @return a boolean.
     * @param job
     */
    boolean canBeMadeBy(final IJob<?> job);

    /**
     * Get the file name of the structure.
     * Calculates the file name from the path.
     * @return the name without the appendix.
     */
    default String getFileName()
    {
        final String[] split = getStructurePath().contains("\\") ? getStructurePath().split("\\\\") : getStructurePath().split("/");
        return split[split.length - 1].replace(".blueprint", "");
    }
}
