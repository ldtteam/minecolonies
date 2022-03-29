package com.minecolonies.api.colony.workorders;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Get the strcuture this work order should be using, if any
     *
     * @return the schematic name.
     */
    String getStructureName();

    int getCurrentLevel();

    int getTargetLevel();

    int getAmountOfResources();

    void setAmountOfResources(int newQuantity);

    String getIteratorType();

    void setIteratorType(String iteratorType);

    boolean isCleared();

    void setCleared(boolean cleared);

    boolean isRequested();

    /**
     * Set whether or not the building has been cleared.
     *
     * @param requested true if the building has been cleared.
     */
    void setRequested(boolean requested);

    /**
     * Checks if the workOrder has changed.
     *
     * @return true if so.
     */
    boolean isChanged();

    /**
     * Resets the changed variable.
     */
    void resetChange();

    String getWorkOrderName();

    WorkOrderType getWorkOrderType();

    /**
     * Get the current location of the building
     *
     * @return the location
     */
    BlockPos getLocation();

    /**
     * Get the current rotation of the building
     *
     * @return the location
     */
    int getRotation();

    /**
     * Whether the current building is mirrored
     *
     * @return the location
     */
    boolean isMirrored();

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
    ITextComponent getDisplayName();

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
     * Read the WorkOrder data from the CompoundNBT.
     *
     * @param compound NBT Tag compound
     * @param manager  the workManager calling this method.
     */
    void read(@NotNull CompoundNBT compound, IWorkManager manager);

    /**
     * Save the Work Order to an CompoundNBT.
     *
     * @param compound NBT tag compount
     */
    void write(@NotNull CompoundNBT compound);

    /**
     * Writes the workOrders data to a byte buf for transition.
     *
     * @param buf Buffer to write to
     */
    void serializeViewNetworkData(@NotNull PacketBuffer buf);

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
     */
    boolean canBeMadeByBuilder();
}
