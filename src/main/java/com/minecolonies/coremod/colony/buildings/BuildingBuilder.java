package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    private final HashMap<String, BuildingBuilder.BuildingBuilderResource> resourcesAvailable = new HashMap<>();

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
     * Information about a resource.
     * - How many are needed to finish the build
     * - How many are available to the builder
     * - How many are in the player's inventory (client side only)
     */
    public static class BuildingBuilderResource
    {
        /**
         * Availability status of the resource.
         * according to the builder's chest, inventory and the player's inventory
         */
        public enum RessourceAvailability
        {
            NOT_NEEDED,
            DONT_HAVE,
            NEED_MORE,
            HAVE_ENOUGH
        }

        private int amountAvailable;
        private int amountPlayer;
        private final String name;
        private final ItemStack itemStack;
        private final int amountNeeded;

        public BuildingBuilderResource(final String name, final ItemStack itemStack, final int amountAvailable, final int amountNeeded)
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

        /**
         * get the amount available for this resource.
         * i.e. amount in the chest + amount in the builder's inventory
         */
        public int getAvailable()
        {
            return amountAvailable;
        }

        public void setAvailable(final int amount)
        {
            amountAvailable = amount;
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
            if (amountNeeded > amountAvailable)
            {
                if (amountPlayer == 0)
                {
                    return RessourceAvailability.DONT_HAVE;
                }
                if (amountPlayer < (amountNeeded-amountAvailable))
                {
                    return RessourceAvailability.NEED_MORE;
                }
                return RessourceAvailability.HAVE_ENOUGH;
            }
            return RessourceAvailability.NOT_NEEDED;
        }

        @Override
        public String toString()
        {
            final int itemId=Item.getIdFromItem(itemStack.getItem());
            final int damage=itemStack.getItemDamage();
            return name + "(p:"+amountPlayer+" a:" +amountAvailable+" n:"+amountNeeded+" id="+itemId+" damage="+damage+") => "+getAvailabilityStatus().name();
        }


        /**
         * Comparator class for BuildingBuilderResource.
         *
         * This is use in the gui to order the list of resources needed.
         */
        public static class ResourceComparator implements Comparator<BuildingBuilderResource>
        {
            /**
             * We want the item availalable in the player inventory first and the one not needed last
             * In alphabetical order otherwise
             */
            @Override
            public int compare(BuildingBuilder.BuildingBuilderResource resource1, BuildingBuilder.BuildingBuilderResource resource2)
            {
                if  (resource1.getAvailabilityStatus()==resource2.getAvailabilityStatus())
                {
                    return resource1.getName().compareTo(resource2.getName());
                }

                return resource2.getAvailabilityStatus().compareTo(resource1.getAvailabilityStatus());
            }
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
        buf.writeInt(neededResources.size());
        for (@NotNull final Map.Entry<String, ItemStack> entry : neededResources.entrySet())
        {
            final BuildingBuilderResource resource = resourcesAvailable.get(entry.getKey());
            //ByteBufUtils.writeItemStack() is Buggy, serialize itemId and damage separately;
            final int itemId = Item.getIdFromItem(resource.itemStack.getItem());
            final int damage = resource.getItemStack().getItemDamage();
            buf.writeInt(itemId);
            buf.writeInt(damage);
            buf.writeInt(resource.getAvailable());
            buf.writeInt(entry.getValue().getCount());
        }
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
     *
     * @param res    the resource.
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
     * Update the available resources.
     * which are needed for the build and in the builder's chest or inventory
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
            final ItemStack itemStack = entry.getValue();
            BuildingBuilderResource resource = resourcesAvailable.get(entry.getKey());
            if (resource == null)
            {
                resource = new BuildingBuilderResource(entry.getValue().getDisplayName(), itemStack,0,itemStack.getCount());
            }

            if (builderInventory!=null)
            {
                resource.setAvailable(resource.getAvailable() 
                    + InventoryUtils.getItemCountInInventory(builderInventory, entry.getValue().getItem(), entry.getValue().getItemDamage()));
            }

            final IInventory chestInventory = this.getTileEntity();
            if (chestInventory!=null)
            {
                resource.setAvailable(resource.getAvailable() 
                    + InventoryUtils.getItemCountInInventory(chestInventory, entry.getValue().getItem(), entry.getValue().getItemDamage()));
            }

            //Count in the additional chests as well
            if (builder!=null)
            {
                for(final BlockPos pos : getAdditionalCountainers())
                {
                    final TileEntity entity = builder.world.getTileEntity(pos);
                    if(entity instanceof TileEntityChest)
                    {
                        resource.setAvailable(resource.getAvailable() 
                            + InventoryUtils.getItemCountInInventory((TileEntityChest)entity, entry.getValue().getItem(), entry.getValue().getItemDamage()));
                    }
                }
            }

            resourcesAvailable.put(entry.getKey(), resource);
        }
    }

    /**
     * Check if the builder requires a certain ItemStack for the current construction.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    public boolean requiresResourceForBuilding(ItemStack stack)
    {
        return neededResources.containsKey(stack.getUnlocalizedName());
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
