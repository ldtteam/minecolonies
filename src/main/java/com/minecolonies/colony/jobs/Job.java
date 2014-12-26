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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Job
{
    private CitizenData citizen;
    private List<ItemStack> itemsNeeded = new ArrayList<ItemStack>();

    //  Job and View Class Mapping
    private static Map<String, Class<? extends Job>> nameToClassMap = new HashMap<String, Class<? extends Job>>();
    private static Map<Class<? extends Job>, String> classToNameMap = new HashMap<Class<? extends Job>, String>();

    private static String TAG_TYPE         = "type";
    private static String TAG_ITEMS_NEEDED = "itemsNeeded";

    static
    {
        addMapping("Placeholder", JobPlaceholder.class);
        addMapping("Builder", JobBuilder.class);
        addMapping("Deliveryman", JobDeliveryman.class);
        addMapping("Miner", JobMiner.class);
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

    public abstract String getName();

    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.CITIZEN;
    }

    public CitizenData getCitizen() { return citizen; }

    public Colony getColony() { return citizen.getColony(); }

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
                MineColonies.logger.error(
                        String.format("A Job %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                                compound.getString(TAG_TYPE), oclass.getName()), ex);
                job = null;
            }
        }
        else
        {
            MineColonies.logger.warn(
                    String.format("Unknown Job type '%s' or missing constructor of proper format.", compound.getString(TAG_TYPE)));
        }

        return job;
    }

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

    public void readFromNBT(NBTTagCompound compound)
    {
        NBTTagList itemsNeededTag = compound.getTagList(TAG_ITEMS_NEEDED, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < itemsNeededTag.tagCount(); i++)
        {
            NBTTagCompound itemCompound = itemsNeededTag.getCompoundTagAt(i);
            itemsNeeded.add(ItemStack.loadItemStackFromNBT(itemCompound));
        }
    }

    public boolean hasItemsNeeded()
    {
        return itemsNeeded.isEmpty();
    }

    public List<ItemStack> getItemsNeeded()
    {
        return itemsNeeded;
    }

    /**
     * Add an item those needed to do the Job
     *
     * @param itemstack
     */
    public void addItemNeeded(ItemStack itemstack)
    {
        boolean isAlreadyNeeded = false;
        for(ItemStack neededItem : itemsNeeded)
        {
            if(itemstack.isItemEqual(neededItem))
            {
                for(int i = 0; i < itemstack.stackSize; i++)
                {
                    neededItem.stackSize++;
                }
                isAlreadyNeeded = true;
            }
        }
        if(!isAlreadyNeeded)
        {
            itemsNeeded.add(itemstack);
        }
    }

    /**
     * Remove an item from those needed to do the Job
     *
     * @param itemstack
     * @return
     */
    public ItemStack removeItemNeeded(ItemStack itemstack)
    {
        ItemStack itemCopy = itemstack.copy();
        for(ItemStack neededItem : itemsNeeded)
        {
            if(itemCopy.isItemEqual(neededItem))
            {
                for(int i = 0; i < itemCopy.stackSize; i++)
                {
                    itemCopy.stackSize--;
                    neededItem.stackSize--;
                    if(neededItem.stackSize == 0)
                    {
                        itemsNeeded.remove(itemsNeeded.indexOf(neededItem));
                        break;
                    }
                }
            }
        }
        return itemCopy.stackSize == 0 ? null : itemstack;
    }

    public void addTasks(EntityAITasks tasks) {}
}
