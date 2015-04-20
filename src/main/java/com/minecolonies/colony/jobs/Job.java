package com.minecolonies.colony.jobs;

import com.minecolonies.MineColonies;
import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class Job
{
    private final CitizenData citizen;
    private List<ItemStack> itemsNeeded = new ArrayList<ItemStack>();

    //  Job and View Class Mapping
    private static Map<String, Class<? extends Job>> nameToClassMap = new HashMap<String, Class<? extends Job>>();
    private static Map<Class<? extends Job>, String> classToNameMap = new HashMap<Class<? extends Job>, String>();

    private static final String TAG_TYPE         = "type";
    private static final String TAG_ITEMS_NEEDED = "itemsNeeded";

    static
    {
        addMapping("Placeholder", JobPlaceholder.class);
        addMapping("Builder", JobBuilder.class);
        addMapping("Deliveryman", JobDeliveryman.class);
        addMapping("Miner", JobMiner.class);
        addMapping("Lumberjack", JobLumberjack.class);
    }

    /**
     * Add a given Job mapping
     *
     * @param name     name of job class
     * @param jobClass class of job
     */
    private static void addMapping(String name, Class<? extends Job> jobClass)
    {
        if (nameToClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Job class mapping");
        }
        else
        {
            try
            {
                if (jobClass.getDeclaredConstructor(CitizenData.class) != null)
                {
                    nameToClassMap.put(name, jobClass);
                    classToNameMap.put(jobClass, name);
                }
            }
            catch (NoSuchMethodException exception)
            {
                throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Job class mapping");
            }
        }
    }

    public Job(CitizenData entity)
    {
        citizen = entity;
    }

    /**
     * Return a Localization label for the Job
     * @return localization label String
     */
    public abstract String getName();

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     * @return
     */
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.CITIZEN;
    }

    /**
     * Get the CitizenData that this Job belongs to
     * @return CitizenData that owns this Job
     */
    public CitizenData getCitizen() { return citizen; }

    /**
     * Get the Colony that this Job is associated with (shortcut for getCitizen().getColony())
     * @return
     */
    public Colony getColony() { return citizen.getColony(); }

    /**
     * Create a Job from saved NBTTagCompound data
     * @param citizen The citizen that owns the Job
     * @param compound The NBTTagCompound containing the saved Job data
     * @return new Job created from the data, or null
     */
    public static Job createFromNBT(CitizenData citizen, NBTTagCompound compound)
    {
        Job job = null;
        Class<? extends Job> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                Constructor<?> constructor = oclass.getDeclaredConstructor(CitizenData.class);
                job = (Job)constructor.newInstance(citizen);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (job != null)
        {
            try
            {
                job.readFromNBT(compound);
            }
            catch (Exception ex)
            {
                MineColonies.logger.error(String.format("A Job %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", compound.getString(TAG_TYPE), oclass.getName()), ex);
                job = null;
            }
        }
        else
        {
            MineColonies.logger.warn(String.format("Unknown Job type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return job;
    }

    /**
     * Save the Job to an NBTTagCompound
     * @param compound NBTTagCompound to save the Job to
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        String s = classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        compound.setString(TAG_TYPE, s);

        if (!itemsNeeded.isEmpty())
        {
            NBTTagList itemsNeededTag = new NBTTagList();
            for (ItemStack itemstack : itemsNeeded)
            {
                NBTTagCompound itemCompound = new NBTTagCompound();
                itemstack.writeToNBT(itemCompound);
                itemsNeededTag.appendTag(itemCompound);
            }
            compound.setTag(TAG_ITEMS_NEEDED, itemsNeededTag);
        }
    }

    /**
     * Restore the Job from an NBTTagCompound
     * @param compound NBTTagCompound containing saved Job data
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        NBTTagList itemsNeededTag = compound.getTagList(TAG_ITEMS_NEEDED, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < itemsNeededTag.tagCount(); i++)
        {
            NBTTagCompound itemCompound = itemsNeededTag.getCompoundTagAt(i);
            itemsNeeded.add(ItemStack.loadItemStackFromNBT(itemCompound));
        }
    }

    /**
     * Does the Job have _all_ the needed items?
     *
     * @return true if the Job has no needed items
     */
    public boolean hasItemsNeeded()
    {
        return !itemsNeeded.isEmpty();
    }

    /**
     * Get the list of items needed by the Job
     *
     * @return List of items needed by the Job
     */
    public List<ItemStack> getItemsNeeded()
    {
        return Collections.unmodifiableList(itemsNeeded);
    }

    /**
     * Add (or increment) an ItemStack to the items needed by the Job
     *
     * @param stack Item+count needed to do the job
     */
    public void addItemNeeded(ItemStack stack)
    {
        for(ItemStack neededItem : itemsNeeded)
        {
            if(stack.isItemEqual(neededItem))
            {
                neededItem.stackSize += stack.stackSize;
                return;
            }
        }

        itemsNeeded.add(stack);
    }

    /**
     * Remove a items from those required to do the Job
     *
     * @param stack ItemStack (item+count) to remove from the list of needed items
     * @return modified ItemStack with remaining items (or null)
     */
    public ItemStack removeItemNeeded(ItemStack stack)
    {
        ItemStack stackCopy = stack.copy();
        for(ItemStack neededItem : itemsNeeded)
        {
            if(stackCopy.isItemEqual(neededItem))
            {
                int itemsToRemove = Math.min(neededItem.stackSize, stackCopy.stackSize);
                neededItem.stackSize -= itemsToRemove;
                stackCopy.stackSize -= itemsToRemove;

                if(neededItem.stackSize == 0)
                {
                    itemsNeeded.remove(neededItem);
                }

                break;
            }
        }

        return stackCopy.stackSize == 0 ? null : stackCopy;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list
     *
     * @param tasks EntityAITasks list to add tasks to
     */
    public void addTasks(EntityAITasks tasks) {}
}
