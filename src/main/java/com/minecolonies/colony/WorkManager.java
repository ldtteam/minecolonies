package com.minecolonies.colony;

import com.minecolonies.colony.workorders.WorkOrder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;

public class WorkManager
{
    protected final Colony colony;
    protected final List<WorkOrder>      orders          = new ArrayList<WorkOrder>();
    protected final Map<UUID, WorkOrder> orderMap        = new HashMap<UUID, WorkOrder>();
//    protected final List<WorkOrder>      unclaimedOrders = new ArrayList<WorkOrder>();
//    protected final List<WorkOrder>      claimedOrders   = new ArrayList<WorkOrder>();

    private final static String TAG_ORDERS = "orders";

    public WorkManager(Colony c)
    {
        colony = c;
    }

    public void addWorkOrder(WorkOrder order)
    {
        orders.add(order);
        orderMap.put(order.getID(), order);
//        if (order.isClaimed())
//        {
//            claimedOrders.add(order);
//        }
//        else
//        {
//            unclaimedOrders.add(order);
//        }
    }

    public void removeWorkOrder(UUID orderId)
    {
        WorkOrder order = orderMap.get(orderId);
        if (order != null)
        {
            removeWorkOrder(order);
        }
    }

    public void removeWorkOrder(WorkOrder order)
    {
        orders.remove(order);
        orderMap.remove(order.getID());
//        claimedOrders.remove(order);
//        unclaimedOrders.remove(order);
    }

    /**
     * Get a work order of the specified id
     * @param id the id of the work order
     * @return the work order of the specified id, or null
     */
    public WorkOrder getWorkOrder(UUID id)
    {
        return orderMap.get(id);
    }

    /**
     * Get a work order of the specified id, as a specific type
     * @param id the id of the work order
     * @param type the class of the expected type of the work order
     * @return the work order of the specified id, or null if it was not found or is of an incompatible type
     */
    public <ORDER extends WorkOrder> ORDER getWorkOrder(UUID id, Class<ORDER> type)
    {
        try
        {
            return type.cast(getWorkOrder(id));
        }
        catch (ClassCastException exc)
        {}

        return null;
    }

    /**
     * Get an unclaimed work order of a specified type
     * @param type the class of the type of work order to find
     * @return an unclaimed work order of the given type, or null if no unclaimed work order of the type was found
     */
    public <ORDER extends WorkOrder> ORDER getAvailableWorkOrder(Class<ORDER> type)
    {
        for (WorkOrder o : orders)
        {
            if (!o.isClaimed() && type.isAssignableFrom(o.getClass()))
            {
                return type.cast(o);
            }
        }

        return null;
    }

    /**
     * Get all work orders of a specified type
     * @param type the class of the type of work order to find
     * @return a list of all work orders of the given type
     */
    public <ORDER extends WorkOrder> List<ORDER> getWorkOrdersOfType(Class<ORDER> type)
    {
        List<ORDER> list = new ArrayList<ORDER>();
        for (WorkOrder o : orders)
        {
            if (type.isAssignableFrom(o.getClass()))
            {
                list.add(type.cast(o));
            }
        }
        return list;
    }

    /**
     * When a citizen is removed, unclaim any Work Orders that were claimed by that citizen
     *
     * @param citizen
     */
    public void clearWorkForCitizen(CitizenData citizen)
    {
        for (WorkOrder o : orders)
        {
            if (o.isClaimedBy(citizen))
            {
                o.removeClaimedBy();
            }
        }
    }

    /**
     * Save the Work Manager
     * @param compound
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        //  Work Orders
        NBTTagList list = new NBTTagList();
        for (WorkOrder o : orders)
        {
            NBTTagCompound orderCompound = new NBTTagCompound();
            o.writeToNBT(orderCompound);
            list.appendTag(orderCompound);
        }
        compound.setTag(TAG_ORDERS, list);
    }

    /**
     * Restore the Work Manager
     * @param compound
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        //  Work Orders
        NBTTagList list = compound.getTagList(TAG_ORDERS, NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); ++i)
        {
            NBTTagCompound orderCompound = list.getCompoundTagAt(i);
            WorkOrder o = WorkOrder.createFromNBT(orderCompound);
            if (o != null)
            {
                addWorkOrder(o);
            }
        }
    }
}
