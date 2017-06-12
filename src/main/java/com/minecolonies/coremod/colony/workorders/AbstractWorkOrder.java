package com.minecolonies.coremod.colony.workorders;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.WorkOrderView;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * General information between WorkOrders.
 */
public abstract class AbstractWorkOrder
{
    private static final String                                          TAG_TYPE       = "type";
    private static final String                                          TAG_ID         = "id";
    private static final String                                          TAG_CLAIMED_BY = "claimedBy";
    //  Job and View Class Mapping
    @NotNull
    private static final Map<String, Class<? extends AbstractWorkOrder>> nameToClassMap = new HashMap<>();
    @NotNull
    private static final Map<Class<? extends AbstractWorkOrder>, String> classToNameMap = new HashMap<>();
    static
    {
        addMapping("build", WorkOrderBuild.class);
        addMapping("decoration", WorkOrderBuildDecoration.class);
    }

    protected int id;
    private   int claimedBy;
    private   int priority;
    private boolean changed = false;

    /**
     * Default constructor; we also start with a new id and replace it during loading;
     * this greatly simplifies creating subclasses.
     */
    public AbstractWorkOrder()
    {
        //Should be overridden
    }

    /**
     * Add a given Work Order mapping.
     *
     * @param name       name of work order
     * @param orderClass class of work order
     */
    private static void addMapping(final String name, @NotNull final Class<? extends AbstractWorkOrder> orderClass)
    {
        if (nameToClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Work Order class mapping");
        }

        try
        {
            if (orderClass.getDeclaredConstructor() != null)
            {
                nameToClassMap.put(name, orderClass);
                classToNameMap.put(orderClass, name);
            }
        }
        catch (final NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Work Order class mapping", exception);
        }
    }

    /**
     * Create a Work Order from a saved NBTTagCompound.
     *
     * @param compound the compound that contains the data for the Work Order
     * @return {@link AbstractWorkOrder} from the NBT
     */
    public static AbstractWorkOrder createFromNBT(@NotNull final NBTTagCompound compound)
    {
        @Nullable AbstractWorkOrder order = null;
        @Nullable Class<? extends AbstractWorkOrder> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                final Constructor<?> constructor = oclass.getDeclaredConstructor();
                order = (AbstractWorkOrder) constructor.newInstance();
            }
        }
        catch (@NotNull NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            Log.getLogger().trace(e);
        }

        if (order == null)
        {
            Log.getLogger().warn(String.format("Unknown WorkOrder type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
            return null;
        }
        try
        {
            order.readFromNBT(compound);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A WorkOrder %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              compound.getString(TAG_TYPE), oclass.getName()), ex);
            return null;
        }

        return order;
    }

    /**
     * Read the WorkOrder data from the NBTTagCompound.
     *
     * @param compound NBT Tag compound
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        id = compound.getInteger(TAG_ID);
        claimedBy = compound.getInteger(TAG_CLAIMED_BY);
    }

    /**
     * Create a WorkOrder View from a buffer.
     *
     * @param buf The network data
     * @return View object of the workOrder
     */
    @Nullable
    public static WorkOrderView createWorkOrderView(final ByteBuf buf)
    {
        @Nullable WorkOrderView workOrderView = new WorkOrderView();

        try
        {
            workOrderView.deserialize(buf);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A WorkOrder.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              workOrderView.getId()), ex);
            workOrderView = null;
        }

        return workOrderView;
    }

    /**
     * Getter for the priority.
     *
     * @return the priority of the work order.
     */
    public int getPriority()
    {
        return this.priority;
    }

    /**
     * Setter for the priority.
     *
     * @param priority the new priority.
     */
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }

    /**
     * Checks if the workOrder has changed.
     *
     * @return true if so.
     */
    public boolean hasChanged()
    {
        return changed;
    }

    /**
     * Resets the changed variable.
     */
    public void resetChange()
    {
        changed = false;
    }

    /**
     * Get the ID of the Work Order.
     *
     * @return ID of the work order
     */
    public int getID()
    {
        return id;
    }

    public void setID(final int id)
    {
        this.id = id;
    }

    /**
     * Is the Work Order claimed?
     *
     * @return true if the Work Order has been claimed
     */
    public boolean isClaimed()
    {
        return claimedBy != 0;
    }

    /**
     * Is the Work Order claimed by the given citizen?
     *
     * @param citizen The citizen to check
     * @return true if the Work Order is claimed by this Citizen
     */
    public boolean isClaimedBy(@NotNull final CitizenData citizen)
    {
        return citizen.getId() == claimedBy;
    }

    /**
     * Get the ID of the Citizen that the Work Order is claimed by.
     *
     * @return ID of citizen the Work Order has been claimed by, or null
     */
    public int getClaimedBy()
    {
        return claimedBy;
    }

    /**
     * Set the Work Order as claimed by the given Citizen.
     *
     * @param citizen {@link CitizenData}
     */
    void setClaimedBy(@Nullable final CitizenData citizen)
    {
        changed = true;
        claimedBy = (citizen != null) ? citizen.getId() : 0;
    }

    /**
     * Clear the Claimed By status of the Work Order.
     */
    public void clearClaimedBy()
    {
        changed = true;
        claimedBy = 0;
    }

    /**
     * Save the Work Order to an NBTTagCompound.
     *
     * @param compound NBT tag compount
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        final String s = classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        compound.setString(TAG_TYPE, s);
        compound.setInteger(TAG_ID, id);
        if (claimedBy != 0)
        {
            compound.setInteger(TAG_CLAIMED_BY, claimedBy);
        }
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     *
     * Suppressing Sonar Rule squid:S1172
     * This rule does " Unused method parameters should be removed"
     * But in this case extending class may need to use the colony parameter
     * @param colony The colony that owns the Work Order
     * @return True if the WorkOrder is still valid, or False if it should be deleted
     */
    @SuppressWarnings("squid:S1172")
    public boolean isValid(final Colony colony)
    {
        return true;
    }

    /**
     * Attempt to fulfill the Work Order.
     * Override this with an implementation for the Work Order to find a Citizen to perform the job
     *
     * @param colony The colony that owns the Work Order
     */
    public abstract void attemptToFulfill(Colony colony);

    /**
     * Writes the workOrders data to a byte buf for transition.
     *
     * @param buf Buffer to write to
     */
    public void serializeViewNetworkData(@NotNull final ByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeInt(priority);
        buf.writeInt(claimedBy);
        buf.writeInt(getType().ordinal());
        ByteBufUtils.writeUTF8String(buf, getValue());
        //value is upgradeName and upgradeLevel for workOrderBuild
    }

    /**
     * Gets of the WorkOrder Type. Overwrite this for the different implementations.
     *
     * @return the type.
     */
    @NotNull
    protected abstract WorkOrderType getType();

    /**
     * Gets the value of the WorkOrder. Overwrite this in every subclass.
     *
     * @return a description string.
     */
    protected abstract String getValue();

    /**
     * Contains all classes which inherit directly from this class.
     */
    public enum WorkOrderType
    {
        BUILD
    }

    /**
     * Executed when a work order is added.
     *
     * Override this when something need to be done when the work order is added
     * @param colony in which the work order exist
     */
    public void onAdded(final Colony colony)
    {
    }

    /**
     * Executed when a work order is completed.
     *
     * Override this when something need to be done when the work order is completed
     * @param colony in which the work order exist
     */
    public void onCompleted(final Colony colony)
    {
    }

    /**
     * Executed when a work order is removed.
     *
     * Override this when something need to be done when the work order is removed
     * @param colony in which the work order exist
     */
    public void onRemoved(final Colony colony)
    {
    }

}
