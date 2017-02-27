package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    private HashMap<String, BuildingBuilderResource> neededResources = new HashMap<>();

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
            final ItemStack stack = ItemStack.loadItemStackFromNBT(neededRes);
            final BuildingBuilderResource resource = new BuildingBuilderResource(stack.getItem(),stack.getItemDamage(), stack.stackSize);
            neededResources.put(stack.getUnlocalizedName(), resource);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList neededResTagList = new NBTTagList();
        for (@NotNull final BuildingBuilderResource resource : neededResources.values())
        {
            @NotNull final NBTTagCompound neededRes = new NBTTagCompound();
            final ItemStack itemStack = new ItemStack(resource.getItem(),resource.getAmount(),resource.getDamageValue());
            itemStack.writeToNBT(neededRes);

            neededResTagList.appendTag(neededRes);
        }
        compound.setTag(TAG_RESOURCE_LIST, neededResTagList);
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
        for (@NotNull final Map.Entry<String, BuildingBuilderResource> entry : neededResources.entrySet())
        {
            final BuildingBuilderResource resource = neededResources.get(entry.getKey());
            //ByteBufUtils.writeItemStack() is Buggy, serialize itemId and damage separately;
            final int itemId = Item.getIdFromItem(resource.getItem());
            final int damage = resource.getDamageValue();
            buf.writeInt(itemId);
            buf.writeInt(damage);
            buf.writeInt(resource.getAvailable());
            buf.writeInt(resource.getAmount());
        }
    }

    /**
     * Get the needed resources for the current build.
     *
     * @return a new Hashmap.
     */
    public Map<String, BuildingBuilderResource> getNeededResources()
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
        if (res == null || res.getItem() == null || res.stackSize == 0 || amount == 0)
        {
            return;
        }
        BuildingBuilderResource resource = this.neededResources.get(res.getUnlocalizedName());
        if (resource == null)
        {
            resource = new BuildingBuilderResource(res.getItem(), res.getItemDamage(), amount);
        }
        else
        {
            resource.setAmount(resource.getAmount()+amount);
        }
        this.neededResources.put(res.getUnlocalizedName(), resource);
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
            preAmount = this.neededResources.get(res.getUnlocalizedName()).getAmount();
        }

        if (preAmount - amount <= 0)
        {
            this.neededResources.remove(res.getUnlocalizedName());
        }
        else
        {
            this.neededResources.get(res.getUnlocalizedName()).setAmount(preAmount - amount);
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
     *
     * which are needed for the build and in the builder's chest or inventory
     */
    private void updateAvailableResources()
    {
        final EntityCitizen builder = getWorkerEntity();

        InventoryCitizen builderInventory = null;
        if (builder!=null)
        {
            builderInventory = builder.getInventoryCitizen();
        }


        for (@NotNull final Map.Entry<String, BuildingBuilderResource> entry : neededResources.entrySet())
        {
            final BuildingBuilderResource resource = entry.getValue();

            resource.setAvailable(0);

            if (builderInventory!=null)
            {
                resource.addAvailable(InventoryUtils.getItemCountInInventory(builderInventory, resource.getItem(), resource.getDamageValue()));
            }

            final IInventory chestInventory = this.getTileEntity();
            if (chestInventory!=null)
            {
                resource.addAvailable(InventoryUtils.getItemCountInInventory(chestInventory, resource.getItem(), resource.getDamageValue()));
            }

            //Count in the additional chests as well
            if (builder!=null)
            {
                for(final BlockPos pos : getAdditionalCountainers())
                {
                    final TileEntity entity = builder.worldObj.getTileEntity(pos);
                    if(entity instanceof TileEntityChest)
                    {
                        resource.addAvailable(InventoryUtils.getItemCountInInventory((TileEntityChest)entity, resource.getItem(), resource.getDamageValue()));
                    }
                }
            }

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
