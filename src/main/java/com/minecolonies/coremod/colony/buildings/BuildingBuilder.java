package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.gui.WindowHutBuilder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.*;

import com.minecolonies.coremod.util.Log;

/**
 * The builders building.
 */
public class BuildingBuilder extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    public static final  int    MAX_BUILDING_LEVEL = 5;
    /**
     * The job description.
     */
    private static final String BUILDER            = "Builder";

    /**
     * Tags to store the needed resourced to nbt.
     */
    private static final String TAG_RESOURCE_LIST = "resourcesItem";

    /**
     * Contains all resources needed for a certain build.
     */
    private HashMap<String, ItemStack> neededResources = new HashMap<>();
    /**
     * Contains all resources available in the chest or builder inventory needed for a certain build.
     */
    private HashMap<String, BuildingBuilder.BuildingBuilderResource> resourcesAvailable = new HashMap<>();

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingBuilder(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter of the schematic name.
     *
     * @return the schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BUILDER;
    }

    /**
     * Getter of the max building level.
     *
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * @see AbstractBuilding#onUpgradeComplete(int)
     */
    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);

        if (newLevel == 1)
        {
            this.getColony().triggerAchievement(ModAchievements.achievementBuildingBuilder);
        }
        if (newLevel >= this.getMaxBuildingLevel())
        {
            this.getColony().triggerAchievement(ModAchievements.achievementUpgradeBuilderMax);
        }
    }

    /**
     * Getter of the job description.
     *
     * @return the description of the builder job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BUILDER;
    }

    /**
     * Create the job for the builder.
     *
     * @param citizen the citizen to take the job.
     * @return the new job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobBuilder(citizen);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList neededResTagList = compound.getTagList(TAG_RESOURCE_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.tagCount(); ++i)
        {
            final NBTTagCompound neededRes = neededResTagList.getCompoundTagAt(i);
            final ItemStack stack = new ItemStack(neededRes);
            neededResources.put(stack.getUnlocalizedName(), stack);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList neededResTagList = new NBTTagList();
        for (@NotNull final ItemStack stack : neededResources.values())
        {
            @NotNull final NBTTagCompound neededRes = new NBTTagCompound();
            stack.writeToNBT(neededRes);

            neededResTagList.appendTag(neededRes);
        }
        compound.setTag(TAG_RESOURCE_LIST, neededResTagList);
    }

    /**
     * Hold the pair of number of avaliable and needed StackItem
     */
    public static class BuildingBuilderResource
    {
        public enum RessourceAvailability
        {
            NOT_NEEDED,
            DONT_HAVE,
            NEED_MORE,
            HAVE_ENOUGHT
        }

        private String name;
        private ItemStack itemStack;
        private int amountAvailable;
        private int amountNeeded;
        private int amountPlayer;

        BuildingBuilderResource(final String name, final ItemStack itemStack, final int amountAvailable, final int amountNeeded)
        {
            this.name = name;
            this.itemStack = itemStack;
            this.amountAvailable = amountAvailable;
            this.amountNeeded = amountNeeded;
            this.amountPlayer = 0;
        }

        public String getName()
        {
            return name;
        }

        public ItemStack getItemStack()
        {
            return itemStack;
        }


        public int getAvailable()
        {
            return amountAvailable;
        }

        public void setAvailable(final int amount)
        {
            amountAvailable=amount;
        }

        public int getNeeded()
        {
            return amountNeeded;
        }

        public void setPlayerAmount(final int amount)
        {
            amountPlayer = amount;
        }

        public int getPlayerAmount()
        {
            return amountPlayer;
        }

        public RessourceAvailability getAvailabilityStatus()
        {
            if (amountNeeded >amountAvailable)
            {
                if (amountPlayer==0)
                {
                    return RessourceAvailability.DONT_HAVE;
                }
                if (amountPlayer<(amountNeeded-amountAvailable))
                {
                    return RessourceAvailability.NEED_MORE;
                }
                return RessourceAvailability.HAVE_ENOUGHT;
            }
            return RessourceAvailability.NOT_NEEDED;
        }

        public String toString()
        {
            final int itemId=Item.getIdFromItem(itemStack.getItem());
            final int damage=itemStack.getItemDamage();
            return name + "(p:"+amountPlayer+" a:" +amountAvailable+" n:"+amountNeeded+" id="+itemId+" damage="+damage+") => "+getAvailabilityStatus().name();
        }
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull ByteBuf buf)
    {
        super.serializeToView(buf);

        updateAvailableResources();
        Log.getLogger().info("SERIALIZE");
        buf.writeInt(neededResources.size());
        for (@NotNull final Map.Entry<String, ItemStack> entry : neededResources.entrySet())
        {
            final BuildingBuilderResource resource = resourcesAvailable.get(entry.getKey());
            //ByteBufUtils.writeUTF8String(buf, entry.getValue().getDisplayName());
            //ByteBufUtils.writeItemStack(buf, resource.getItemStack());
            final int itemId = Item.getIdFromItem(resource.itemStack.getItem());
            final int damage = resource.getItemStack().getItemDamage();
            Log.getLogger().info("itemId="+itemId+" damage="+damage);
            buf.writeInt(itemId);
            buf.writeInt(damage);
            buf.writeInt(resource.getAvailable());
            buf.writeInt(entry.getValue().getCount());
            Log.getLogger().info(resource.toString());
        }
            Log.getLogger().info("------------------------");

    }

    /**
     * Get the needed resources for the current build.
     *
     * @return a new Hashmap.
     */
    public Map<String, ItemStack> getNeededResources()
    {
        return new HashMap<>(neededResources);
    }

    /**
     * Add a new resource to the needed list.
     *  @param res    the resource.
     * @param amount the amount.
     */
    public void addNeededResource(@Nullable final ItemStack res, final int amount)
    {
        int preAmount = 0;
        if (this.neededResources.containsKey(res.getUnlocalizedName()))
        {
            preAmount = this.neededResources.get(res.getUnlocalizedName()).getCount();
        }
        res.setCount(preAmount + amount);
        this.neededResources.put(res.getUnlocalizedName(), res);
        this.markDirty();
    }

    /**
     * Can be overriden by implementations to specify which tools are useful for the worker.
     * When dumping he will keep these.
     *
     * @param stack the stack to decide on
     * @return if should be kept or not.
     */
    @Override
    public boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }

    /**
     * Reduce a resource of the needed list.
     *
     * @param res    the resource.
     * @param amount the amount.
     */
    public void reduceNeededResource(final ItemStack res, final int amount)
    {
        int preAmount = 0;
        if (this.neededResources.containsKey(res.getUnlocalizedName()))
        {
            preAmount = this.neededResources.get(res.getUnlocalizedName()).getCount();
        }

        if (preAmount - amount <= 0)
        {
            this.neededResources.remove(res.getUnlocalizedName());
        }
        else
        {
            this.neededResources.get(res.getUnlocalizedName()).setCount(preAmount - amount);
        }
        this.markDirty();
    }

    /**
     * Resets the needed resources completely.
     */
    public void resetNeededResources()
    {
        neededResources = new HashMap<>();
        this.markDirty();
    }

    /**
     * Update the available resources (from the chest or builder's inventory)
     * which are needed for the build
     */
    private void updateAvailableResources()
    {
        final EntityCitizen builder = getWorkerEntity();
        resourcesAvailable.clear();


        InventoryCitizen builderInventory = null;
        if (builder!=null)
        {
            builderInventory = builder.getInventoryCitizen();
        }


        for (@NotNull final Map.Entry<String, ItemStack> entry : neededResources.entrySet())
        {
            ItemStack itemStack = entry.getValue();
            Log.getLogger().info("Need "+itemStack.getDisplayName());
            BuildingBuilderResource resource = resourcesAvailable.get(entry.getKey());
            if (resource == null)
            {
                resource = new BuildingBuilderResource(entry.getValue().getDisplayName(), itemStack,0,itemStack.getCount());
            }

            if (builderInventory!=null)
            {
                resource.setAvailable(resource.getAvailable() + InventoryUtils.getItemCountInInventory(builderInventory, entry.getValue().getItem(), entry.getValue().getItemDamage()));
            }

            final IInventory chestInventory = this.getTileEntity();
            if (chestInventory!=null)
            {
                resource.setAvailable(resource.getAvailable() + InventoryUtils.getItemCountInInventory(chestInventory, entry.getValue().getItem(), entry.getValue().getItemDamage()));
            }

            //Count in the additional chests as well
            if (builder!=null)
            {
                for(final BlockPos pos : getAdditionalCountainers())
                {
                    final TileEntity entity = builder.world.getTileEntity(pos);
                    if(entity instanceof TileEntityChest)
                    {
                        resource.setAvailable(resource.getAvailable() + InventoryUtils.getItemCountInInventory((TileEntityChest)entity, entry.getValue().getItem(), entry.getValue().getItemDamage()));
                    }
                }
            }

            resourcesAvailable.put(entry.getKey(), resource);
        }
    }

    /*
     * Check if the builder requires a certain ItemStack for the current construction.
     * @param stack the stack to test.
     * @return true if so.
     */
    public boolean requiresResourceForBuilding(ItemStack stack)
    {
        return neededResources.containsKey(stack.getUnlocalizedName());
    }

    /**
     * Provides a view of the builder building class.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        private final HashMap<String, BuildingBuilder.BuildingBuilderResource> resources = new HashMap<>();

        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Gets the blockOut Window.
         *
         * @return the window of the builder building.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutBuilder(this);
        }

        @Override
        public void deserialize(@NotNull ByteBuf buf)
        {
            super.deserialize(buf);

            final int size = buf.readInt();
            resources.clear();
            Log.getLogger().info("DESERIALIZE");

            for (int i = 0; i < size; i++)
            {
                //Serialising the ItemStack give a bad ItemStack sometimes using itemId + damage instead
                //final ItemStack itemStack = ByteBufUtils.readItemStack(buf);
                final int itemId = buf.readInt();
                final int damage = buf.readInt();
                final ItemStack itemStack = new ItemStack(Item.getByNameOrId(Integer.toString(itemId)),1,damage);
                Log.getLogger().info("itemId="+itemId+" damage="+damage);
                final int amountAvailable = buf.readInt();
                final int amountNeeded = buf.readInt();

                final BuildingBuilderResource resource = new BuildingBuilderResource(itemStack.getDisplayName(), itemStack, amountAvailable,amountNeeded);
                Log.getLogger().info(resource.toString());
                resources.put(itemStack.getDisplayName(), resource);
            }
            Log.getLogger().info("++++++++++++++++++");
        }

        /**
         * Getter for the needed resources.
         *
         * @return a copy of the HashMap(String, Object).
         */

        public Map<String, BuildingBuilder.BuildingBuilderResource> getResources()
        {
            return Collections.unmodifiableMap(resources);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }

    @Override
    public boolean transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        if (super.transferStack(stack, world))
        {
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        final ItemStack itemStack = super.forceTransferStack(stack, world);
        if (itemStack != null)
        {
            this.markDirty();
            return itemStack;
        }
        return itemStack;
    }
}
