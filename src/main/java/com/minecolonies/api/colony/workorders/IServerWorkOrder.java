package com.minecolonies.api.colony.workorders;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Suppression.UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED;

public interface IServerWorkOrder extends IWorkOrder
{
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
     * Whether the workorder can be built by the given building.
     *
     * @param building the building.
     * @return
     */
    boolean canBuild(IBuilding building);
}
