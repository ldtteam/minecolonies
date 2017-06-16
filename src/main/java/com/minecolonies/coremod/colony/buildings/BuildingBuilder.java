package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

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
        return ItemStackUtils.hasToolLevel(stack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel())
            || ItemStackUtils.hasToolLevel(stack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel())
            || ItemStackUtils.hasToolLevel(stack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel())
            || neededResources.containsKey(stack.getUnlocalizedName());
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
            final BuildingBuilderResource resource = new BuildingBuilderResource(stack, ItemStackUtils.getSize(stack));
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
            ByteBufUtils.writeItemStack(buf, resource.getItemStack());
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
        if (ItemStackUtils.isEmpty(res) || amount == 0)
        {
            return;
        }
        BuildingBuilderResource resource = this.neededResources.get(res.getUnlocalizedName());
        if (resource == null)
        {
            resource = new BuildingBuilderResource(res, amount);
        }
        else
        {
            resource.setAmount(resource.getAmount()+amount);
        }
        this.neededResources.put(res.getUnlocalizedName(), resource);
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
                resource.addAvailable(InventoryUtils.getItemCountInItemHandler(new InvWrapper(builderInventory), resource.getItem(), resource.getDamageValue()));
            }

            final TileEntity chestInventory = this.getTileEntity();
            if (chestInventory!=null)
            {
                resource.addAvailable(InventoryUtils.getItemCountInProvider(chestInventory, resource.getItem(), resource.getDamageValue()));
            }

            //Count in the additional chests as well
            if (builder!=null)
            {
                for(final BlockPos pos : getAdditionalCountainers())
                {
                    final TileEntity entity = CompatibilityUtils.getWorld(builder).getTileEntity(pos);
                    if(entity instanceof TileEntityChest)
                    {
                        resource.addAvailable(InventoryUtils.getItemCountInProvider(entity, resource.getItem(), resource.getDamageValue()));
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
    public ItemStack transferStack(@NotNull final ItemStack stack, @NotNull final World world)
    {
        @NotNull final ItemStack resultStack = super.transferStack(stack, world);

        if (ItemStackUtils.isEmpty(resultStack))
        {
            this.markDirty();
        }

        return resultStack;
    }

    @Override
    public ItemStack forceTransferStack(final ItemStack stack, final World world)
    {
        final ItemStack itemStack = super.forceTransferStack(stack, world);
        if (ItemStackUtils.isEmpty(itemStack))
        {
            this.markDirty();
        }

        return itemStack;
    }
}
