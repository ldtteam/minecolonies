package com.minecolonies.api.colony.workorders;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IWorkManager
{
    /**
     * Removes a work order from the work manager.
     *
     * @param order {@link IWorkOrder} to remove.
     */
    void removeWorkOrder(@NotNull IWorkOrder order);

    /**
     * Removes a work order from the work manager.
     *
     * @param orderId ID of the order to remove
     */
    void removeWorkOrder(int orderId);

    /**
     * Get a work order of the specified id, as a specific type.
     *
     * @param id   the id of the work order.
     * @param type the class of the expected type of the work order.
     * @param <W>  the type of work order to return.
     * @return the work order of the specified id, or null if it was not found
     * or is of an incompatible type.
     */
    @Nullable
    <W extends IWorkOrder> W getWorkOrder(int id, @NotNull Class<W> type);

    /**
     * Get a work order of the specified id.
     *
     * @param id the id of the work order.
     * @return the work order of the specified id, or null.
     */
    IWorkOrder getWorkOrder(int id);

    /**
     * Get an unclaimed work order of a specified type.
     *
     * @param type the class of the type of work order to find.
     * @param <W>  the type of work order to return.
     * @return an unclaimed work order of the given type, or null if no
     * unclaimed work order of the type was found.
     */
    @Nullable
    <W extends IWorkOrder> W getUnassignedWorkOrder(@NotNull Class<W> type);

    /**
     * Get all work orders of a specified type.
     *
     * @param type the class of the type of work order to find.
     * @param <W>  the type of work order to return.
     * @return a list of all work orders of the given type.
     */
    <W extends IWorkOrder> List<W> getWorkOrdersOfType(@NotNull Class<W> type);

    /**
     * Get all work orders.
     *
     * @return a list of all work orders.
     */
    @NotNull
    Map<Integer, IWorkOrder> getWorkOrders();

    /**
     * When a citizen is removed, unclaim any Work Orders that were claimed by
     * that citizen.
     *
     * @param citizen Citizen to unclaim work for.
     */
    void clearWorkForCitizen(@NotNull ICitizenData citizen);

    /**
     * Save the Work Manager.
     *
     * @param compound Compound to save to.
     */
    void writeToNBT(@NotNull NBTTagCompound compound);

    /**
     * Restore the Work Manager.
     *
     * @param compound Compound to read from.
     */
    void readFromNBT(@NotNull NBTTagCompound compound);

    /**
     * Adds work order to the work manager.
     *
     * @param order          Order to add.
     * @param readingFromNbt if being read from NBT.
     */
    void addWorkOrder(@NotNull IWorkOrder order, boolean readingFromNbt);

    /**
     * Process updates on the World Tick.
     * Currently, does periodic Work Order cleanup.
     *
     * @param colony {@link TickEvent.WorldTickEvent}.
     */
    void onColonyTick(@NotNull IColony colony);

    /**
     * Get an ordered list by priority of the work orders.
     *
     * @param type the type of workOrder which is required.
     * @param builder the builder wanting to claim it.
     * @return the list.
     */
    <W extends IWorkOrder> List<W> getOrderedList(@NotNull Class<W> type, BlockPos builder);

    /**
     * Checks if changes has been made.
     *
     * @return true if so.
     */
    boolean isDirty();

    /**
     * Sets if changes has been made.
     *
     * @param dirty true if so. False to reset.
     */
    void setDirty(boolean dirty);

    IColony getColony();
}
