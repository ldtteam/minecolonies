package com.minecolonies.colony.workorders;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.jobs.Job;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkOrder
{
    protected UUID id;
    protected UUID claimedBy;

    //  Job and View Class Mapping
    private static Map<String, Class<? extends WorkOrder>> nameToClassMap = new HashMap<String, Class<? extends WorkOrder>>();
    private static Map<Class<? extends WorkOrder>, String> classToNameMap = new HashMap<Class<? extends WorkOrder>, String>();

    private final static String TAG_TYPE = "type";
    private final static String TAG_ID   = "id";
    private final static String TAG_CLAIMED_BY = "claimedBy";

    static
    {
        addMapping("build", WorkOrderBuild.class);
    }

    /**
     * Add a given Work Order mapping
     *
     * @param name       name of work order
     * @param orderClass class of work order
     */
    private static void addMapping(String name, Class<? extends WorkOrder> orderClass)
    {
        if (nameToClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Work Order class mapping");
        }
        else
        {
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
                throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Work Order class mapping");
            }
        }
    }

    public WorkOrder()
    {
        id = UUID.randomUUID();
    }

    public UUID getID()
    {
        return id;
    }

    public boolean isClaimed()
    {
        return claimedBy != null;
    }

    public boolean isClaimedBy(UUID uuid)
    {
        return uuid.equals(claimedBy);
    }

    public boolean isClaimedBy(CitizenData citizen)
    {
        return isClaimedBy(citizen.getId());
    }

    public UUID getClaimedBy()
    {
        return claimedBy;
    }

    public void setClaimedBy(UUID c)
    {
        this.claimedBy = c;
    }

    public void setClaimedBy(CitizenData citizen)
    {
        setClaimedBy(citizen.getId());
    }

    public void setClaimedBy(Job job)
    {
        setClaimedBy(job.getCitizen());
    }

    public void removeClaimedBy()
    {
        claimedBy = null;
    }

    public static WorkOrder createFromNBT(NBTTagCompound compound)
    {
        WorkOrder order = null;
        Class<? extends WorkOrder> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                Constructor<?> constructor = oclass.getDeclaredConstructor();
                order = (WorkOrder) constructor.newInstance();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (order != null)
        {
            try
            {
                order.readFromNBT(compound);
            }
            catch (Exception ex)
            {
                MineColonies.logger.error(String.format("A WorkOrder %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", compound.getString(TAG_TYPE), oclass.getName()), ex);
                order = null;
            }
        }
        else
        {
            MineColonies.logger.warn(String.format("Unknown Building type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return order;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        String s = classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        compound.setString(TAG_TYPE, s);
        compound.setString(TAG_ID, id.toString());
        if (claimedBy != null)
        {
            compound.setString(TAG_CLAIMED_BY, claimedBy.toString());
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        id = UUID.fromString(compound.getString(TAG_ID));
        if (compound.hasKey(TAG_CLAIMED_BY))
        {
            claimedBy = UUID.fromString(compound.getString(TAG_CLAIMED_BY));
        }
    }
}
