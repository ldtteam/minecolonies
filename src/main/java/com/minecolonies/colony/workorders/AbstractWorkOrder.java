package com.minecolonies.colony.workorders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.util.Log;

import net.minecraft.nbt.NBTTagCompound;

/**
 * General information between WorkOrders.
 */
public abstract class AbstractWorkOrder
{
    protected int id;
    private int claimedBy;

    //  Job and View Class Mapping
    private static          Map<String, Class<? extends AbstractWorkOrder>> nameToClassMap  = new HashMap<>();
    private static          Map<Class<? extends AbstractWorkOrder>, String> classToNameMap  = new HashMap<>();

    private static final String TAG_TYPE = "type";
    private static final String TAG_ID = "id";
    private static final String TAG_CLAIMED_BY = "claimedBy";

    static
    {
        addMapping("build", WorkOrderBuild.class);
        addMapping("decoration", WorkOrderBuildDecoration.class);
    }

    /**
     * Default constructor; we also start with a new id and replace it during loading;
     * this greatly simplifies creating subclasses
     */
    public AbstractWorkOrder()
    {
        //Should be overridden
    }

    /**
     * Add a given Work Order mapping
     *
     * @param name       name of work order
     * @param orderClass class of work order
     */
    private static void addMapping(String name, Class<? extends AbstractWorkOrder> orderClass)
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
        catch (NoSuchMethodException exception)
        {
            throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Work Order class mapping", exception);
        }
    }

    /**
     * Get the ID of the Work Order
     *
     * @return          ID of the work order
     */
    public int getID()
    {
        return id;
    }

    public void setID(int id)
    {
        this.id = id;
    }

    /**
     * Is the Work Order claimed?
     *
     * @return          true if the Work Order has been claimed
     */
    public boolean isClaimed()
    {
        return claimedBy != 0;
    }

    /**
     * Is the Work Order claimed by the given citizen?
     *
     * @param citizen   The citizen to check
     * @return          true if the Work Order is claimed by this Citizen
     */
    public boolean isClaimedBy(CitizenData citizen)
    {
        return citizen.getId() == claimedBy;
    }

    /**
     * Get the ID of the Citizen that the Work Order is claimed by
     *
     * @return          ID of citizen the Work Order has been claimed by, or null
     */
    public int getClaimedBy()
    {
        return claimedBy;
    }

    /**
     * Set the Work Order as claimed by the given Citizen
     *
     * @param citizen   {@link CitizenData}
     */
    void setClaimedBy(CitizenData citizen)
    {
        claimedBy = (citizen != null) ? citizen.getId() : 0;
    }

    /**
     * Clear the Claimed By status of the Work Order
     */
    public void clearClaimedBy()
    {
        claimedBy = 0;
    }

    /**
     * Create a Work Order from a saved NBTTagCompound
     *
     * @param compound      the compound that contains the data for the Work Order
     * @return              {@link AbstractWorkOrder} from the NBT
     */
    public static AbstractWorkOrder createFromNBT(NBTTagCompound compound)
    {
        AbstractWorkOrder order = null;
        Class<? extends AbstractWorkOrder> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                Constructor<?> constructor = oclass.getDeclaredConstructor();
                order = (AbstractWorkOrder) constructor.newInstance();
            }
        }
        catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            Log.logger.trace(e);
        }

        if (order != null)
        {
            try
            {
                order.readFromNBT(compound);
            }
            catch (RuntimeException ex)
            {
                Log.logger.error(String.format("A WorkOrder %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                        compound.getString(TAG_TYPE), oclass.getName()), ex);
                order = null;
            }
        }
        else
        {
            Log.logger.warn(String.format("Unknown WorkOrder type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return order;
    }

    /**
     * Save the Work Order to an NBTTagCompound
     *
     * @param compound  NBT tag compount
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        String s = classToNameMap.get(this.getClass());

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
     * Read the WorkOrder data from the NBTTagCompound
     *
     * @param compound  NBT Tag compound
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        id = compound.getInteger(TAG_ID);
        claimedBy = compound.getInteger(TAG_CLAIMED_BY);
    }

    /**
     * Is this WorkOrder still valid?  If not, it will be deleted.
     *
     * @param colony    The colony that owns the Work Order
     * @return True if the WorkOrder is still valid, or False if it should be deleted
     */
    public boolean isValid(Colony colony)
    {
        return true;
    }

    /**
     * Attempt to fulfill the Work Order.
     * Override this with an implementation for the Work Order to find a Citizen to perform the job
     *
     * @param colony    The colony that owns the Work Order
     */
    public abstract void attemptToFulfill(Colony colony);
}
