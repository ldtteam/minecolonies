package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles work orders for a colony.
 */
public class WorkManager
{
    private static final String TAG_WORK_ORDERS              = "workOrders";
    //  Once a second
    private static final int    WORK_ORDER_FULFILL_INCREMENT = 1 * 20;
    /**
     * The Colony the workManager takes part of.
     */
    protected final Colony colony;
    @NotNull
    private final Map<Integer, AbstractWorkOrder> workOrders     = new LinkedHashMap<>();
    private       int                             topWorkOrderId = 0;
    /**
     * Checks if there has been changes.
     */
    private       boolean                         dirty          = false;

    /**
     * Constructor, saves reference to the colony.
     *
     * @param c Colony the work manager is for.
     */
    public WorkManager(final Colony c)
    {
        colony = c;
    }

    /**
     * Removes a work order from the work manager.
     *
     * @param order {@link AbstractWorkOrder} to remove.
     */
    public void removeWorkOrder(@NotNull final AbstractWorkOrder order)
    {
        removeWorkOrder(order.getID());
    }

    /**
     * Removes a work order from the work manager.
     *
     * @param orderId ID of the order to remove
     */
    public void removeWorkOrder(final int orderId)
    {
        final AbstractWorkOrder workOrder = workOrders.get(orderId);
        workOrders.remove(orderId);
        colony.removeWorkOrder(orderId);
        workOrder.onRemoved(colony);
    }

    /**
     * Get a work order of the specified id, as a specific type.
     *
     * @param id   the id of the work order.
     * @param type the class of the expected type of the work order.
     * @param <W>  the type of work order to return.
     * @return the work order of the specified id, or null if it was not found or is of an incompatible type.
     */
    @Nullable
    public <W extends AbstractWorkOrder> W getWorkOrder(final int id, @NotNull final Class<W> type)
    {
        final AbstractWorkOrder workOrder = getWorkOrder(id);
        if (type.isInstance(workOrder))
        {
            return type.cast(workOrder);
        }

        return null;
    }

    /**
     * Get a work order of the specified id.
     *
     * @param id the id of the work order.
     * @return the work order of the specified id, or null.
     */
    public AbstractWorkOrder getWorkOrder(final int id)
    {
        return workOrders.get(id);
    }

    /**
     * Get an unclaimed work order of a specified type.
     *
     * @param type the class of the type of work order to find.
     * @param <W>  the type of work order to return.
     * @return an unclaimed work order of the given type, or null if no unclaimed work order of the type was found.
     */
    @Nullable
    public <W extends AbstractWorkOrder> W getUnassignedWorkOrder(@NotNull final Class<W> type)
    {
        for (@NotNull final AbstractWorkOrder o : workOrders.values())
        {
            if (!o.isClaimed() && type.isInstance(o))
            {
                return type.cast(o);
            }
        }

        return null;
    }

    /**
     * Get all work orders of a specified type.
     *
     * @param type the class of the type of work order to find.
     * @param <W>  the type of work order to return.
     * @return a list of all work orders of the given type.
     */
    public <W extends AbstractWorkOrder> List<W> getWorkOrdersOfType(@NotNull final Class<W> type)
    {
        return workOrders.values().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    /**
     * Get all work orders.
     *
     * @return a list of all work orders.
     */
    @NotNull
    public Map<Integer, AbstractWorkOrder> getWorkOrders()
    {
        return workOrders;
    }

    /**
     * When a citizen is removed, unclaim any Work Orders that were claimed by that citizen.
     *
     * @param citizen Citizen to unclaim work for.
     */
    public void clearWorkForCitizen(@NotNull final CitizenData citizen)
    {
        dirty = true;
        workOrders.values().stream().filter(o -> o != null && o.isClaimedBy(citizen)).forEach(AbstractWorkOrder::clearClaimedBy);
    }

    /**
     * Save the Work Manager.
     *
     * @param compound Compound to save to.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        //  Work Orders
        @NotNull final NBTTagList list = new NBTTagList();
        for (@NotNull final AbstractWorkOrder o : workOrders.values())
        {
            @NotNull final NBTTagCompound orderCompound = new NBTTagCompound();
            o.writeToNBT(orderCompound);
            list.appendTag(orderCompound);
        }
        compound.setTag(TAG_WORK_ORDERS, list);
    }

    /**
     * Restore the Work Manager.
     *
     * @param compound Compound to read from.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        //  Work Orders
        final NBTTagList list = compound.getTagList(TAG_WORK_ORDERS, NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); ++i)
        {
            final NBTTagCompound orderCompound = list.getCompoundTagAt(i);
            @Nullable final AbstractWorkOrder o = AbstractWorkOrder.createFromNBT(orderCompound);
            if (o != null)
            {
                addWorkOrder(o);

                //  If this Work Order is claimed, and the Citizen who claimed it no longer exists
                //  then clear the Claimed status
                //  This is just a failsafe cleanup; this should not happen under normal circumstances
                if (o.isClaimed() && colony.getCitizen(o.getClaimedBy()) == null)
                {
                    o.clearClaimedBy();
                }

                topWorkOrderId = Math.max(topWorkOrderId, o.getID());
            }
        }
    }

    /**
     * Adds work order to the work manager.
     *
     * @param order Order to add.
     */
    public void addWorkOrder(@NotNull final AbstractWorkOrder order)
    {
        dirty = true;

        if (order.getID() == 0)
        {
            topWorkOrderId++;
            order.setID(topWorkOrderId);
        }

        workOrders.put(order.getID(), order);
        order.onAdded(colony);
    }

    /**
     * Process updates on the World Tick.
     * Currently, does periodic Work Order cleanup.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}.
     */
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            @NotNull final Iterator<AbstractWorkOrder> iter = workOrders.values().iterator();
            while (iter.hasNext())
            {
                final AbstractWorkOrder o = iter.next();
                if (!o.isValid(colony))
                {
                    iter.remove();
                    dirty = true;
                }
                else if (o.hasChanged())
                {
                    dirty = true;
                    o.resetChange();
                }
            }

            if ((event.world.getWorldTime() % WORK_ORDER_FULFILL_INCREMENT) == 0)
            {
                workOrders.values().stream().filter(o -> !o.isClaimed())
                  .sorted(Comparator.comparingInt(AbstractWorkOrder::getPriority).reversed())
                  .forEach(o -> o.attemptToFulfill(colony));
            }
        }
    }

    /**
     * Checks if changes has been made.
     *
     * @return true if so.
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Sets if changes has been made.
     *
     * @param dirty true if so. False to reset.
     */
    public void setDirty(final boolean dirty)
    {
        this.dirty = dirty;
    }
}
