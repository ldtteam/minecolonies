package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.util.StructureIterator;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * The structureBuilder building.
 */
public abstract class AbstractBuildingStructureBuilder extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    public static final  int    MAX_BUILDING_LEVEL = 5;

    /**
     * Tags to store the needed resourced to nbt.
     */
    private static final String TAG_RESOURCE_LIST = "resourcesItem";

    /**
     * Tags to store the needed resourced to nbt.
     */
    private static final String TAG_PROGRESS_POS = "progressPos";

    /**
     * Tags to store the needed resourced to nbt.
     */
    private static final String TAG_PROGRESS_STAGE = "progressStage";

    /**
     * Progress amount to mark building dirty.
     */
    private static final int COUNT_TO_STORE_POS  = 50;

    /**
     * Progress position of the builder.
     */
    private BlockPos progressPos;

    /**
     * Progress stage of the builder.
     */
    private StructureIterator.Stage progressStage;

    /**
     * Contains all resources needed for a certain build.
     */
    private Map<String, BuildingBuilderResource> neededResources = new LinkedHashMap<>();

    /**
     * The progress counter of the builder.
     */
    private int progressCounter = 0;

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public AbstractBuildingStructureBuilder(final Colony c, final BlockPos l)
    {
        super(c, l);
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

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        for (final BuildingBuilderResource stack : neededResources.values())
        {
            toKeep.put(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, true), new Tuple<>(stack.getAmount(), true));
        }

        return toKeep;
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

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList neededResTagList = compound.getTagList(TAG_RESOURCE_LIST, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.tagCount(); ++i)
        {
            final NBTTagCompound neededRes = neededResTagList.getCompoundTagAt(i);
            final ItemStack stack = new ItemStack(neededRes);
            final BuildingBuilderResource resource = new BuildingBuilderResource(stack, ItemStackUtils.getSize(stack));
            final int hashCode = stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0;
            neededResources.put(stack.getTranslationKey() + ":" + stack.getItemDamage() + "-" + hashCode, resource);
        }

        if (compound.hasKey(TAG_PROGRESS_POS))
        {
            progressPos = BlockPosUtil.readFromNBT(compound, TAG_PROGRESS_POS);
            progressStage = StructureIterator.Stage.values()[compound.getInteger(TAG_PROGRESS_STAGE)];
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
            final ItemStack itemStack = new ItemStack(resource.getItem(), resource.getAmount(), resource.getDamageValue());
            itemStack.setTagCompound(resource.getItemStack().getTagCompound());
            itemStack.writeToNBT(neededRes);

            neededResTagList.appendTag(neededRes);
        }

        compound.setTag(TAG_RESOURCE_LIST, neededResTagList);
        if (progressPos != null)
        {
            BlockPosUtil.writeToNBT(compound, TAG_PROGRESS_POS, progressPos);
            compound.setInteger(TAG_PROGRESS_STAGE, progressStage.ordinal());
        }
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        updateAvailableResources();
        buf.writeInt(neededResources.size());
        double qty = 0;
        for (@NotNull final BuildingBuilderResource resource : neededResources.values())
        {
            ByteBufUtils.writeItemStack(buf, resource.getItemStack());
            buf.writeInt(resource.getAvailable());
            buf.writeInt(resource.getAmount());
            qty += resource.getAmount();
        }

        final CitizenData data = this.getMainCitizen();
        if(data != null && data.getJob() instanceof AbstractJobStructure)
        {
            final AbstractJobStructure structureBuilderJob = (AbstractJobStructure) data.getJob();
            final WorkOrderBuildDecoration workOrderBuildDecoration = structureBuilderJob.getWorkOrder();
            if(workOrderBuildDecoration != null)
            {
                final BlockPos pos = workOrderBuildDecoration.getBuildingLocation();
                final String name =
                        workOrderBuildDecoration instanceof WorkOrderBuild ? ((WorkOrderBuild) workOrderBuildDecoration).getUpgradeName() : workOrderBuildDecoration.getName();
                ByteBufUtils.writeUTF8String(buf, name);

                final String desc;
                if(pos.equals(getLocation()))
                {
                    desc = "here";
                }
                else
                {
                    final BlockPos relativePos = getLocation().subtract(pos);
                    final EnumFacing facingX = EnumFacing.getFacingFromVector(relativePos.getX(), 0, 0);
                    final EnumFacing facingZ = EnumFacing.getFacingFromVector(0, 0, relativePos.getZ());
                    desc = relativePos.getX() + " " + facingX + " " + relativePos.getZ() + " " + facingZ;
                }

                ByteBufUtils.writeUTF8String(buf, desc);
                buf.writeDouble(workOrderBuildDecoration.getAmountOfRes() == 0 ? 0 : qty/workOrderBuildDecoration.getAmountOfRes());
            }
            else
            {
                ByteBufUtils.writeUTF8String(buf, "-");
                ByteBufUtils.writeUTF8String(buf, "");
                buf.writeDouble(0.0);
            }
        }
        else
        {
            ByteBufUtils.writeUTF8String(buf, "-");
            ByteBufUtils.writeUTF8String(buf, "");
            buf.writeDouble(0.0);
        }
        ByteBufUtils.writeUTF8String(buf, (getMainCitizen() == null || colony.getCitizenManager().getCitizen(getMainCitizen().getId()) == null) ? "" : getMainCitizen().getName());

    }

    /**
     * Update the available resources.
     * <p>
     * which are needed for the build and in the structureBuilder's chest or inventory
     */
    private void updateAvailableResources()
    {

        getMainCitizenEntity().ifPresent(structureBuilder -> {
            final InventoryCitizen structureBuilderInventory = getMainCitizen().getInventory();
            if (structureBuilderInventory == null)
            {
                return;
            }

            for (@NotNull final Map.Entry<String, BuildingBuilderResource> entry : neededResources.entrySet())
            {
                final BuildingBuilderResource resource = entry.getValue();

                resource.setAvailable(0);

                if (structureBuilderInventory != null)
                {

                    resource.addAvailable(InventoryUtils.getItemCountInItemHandler(new InvWrapper(structureBuilderInventory),
                            stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resource.getItemStack(), true, true)));
                }

                if (getTileEntity() != null)
                {
                    resource.addAvailable(InventoryUtils.getItemCountInItemHandler(this.getCapability(ITEM_HANDLER_CAPABILITY, null),
                      stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resource.getItemStack(), true, true)));
                }
            }
        });
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
     *
     * @param res    the resource.
     * @param amount the amount.
     */
    public void addNeededResource(@Nullable final ItemStack res, final int amount)
    {
        if (ItemStackUtils.isEmpty(res) || amount == 0)
        {
            return;
        }
        final int hashCode = res.hasTagCompound() ? res.getTagCompound().hashCode() : 0;
        BuildingBuilderResource resource = this.neededResources.get(res.getTranslationKey() + ":" + res.getItemDamage() + "-" + hashCode);
        if (resource == null)
        {
            resource = new BuildingBuilderResource(res, amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        this.neededResources.put(res.getTranslationKey() + ":" + res.getItemDamage() + "-" + hashCode, resource);
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
        final int hashCode = res.hasTagCompound() ? res.getTagCompound().hashCode() : 0;
        int preAmount = 0;
        final String name = res.getTranslationKey() + ":" + res.getItemDamage() + "-" + hashCode;
        if (this.neededResources.containsKey(name))
        {
            preAmount = this.neededResources.get(name).getAmount();
        }

        if (preAmount - amount <= 0)
        {
            this.neededResources.remove(name);
        }
        else
        {
            this.neededResources.get(name).setAmount(preAmount - amount);
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
     * Check if the structureBuilder requires a certain ItemStack for the current construction.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    public boolean requiresResourceForBuilding(final ItemStack stack)
    {
        final int hashCode = stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0;
        return neededResources.containsKey(stack.getTranslationKey() + ":" + stack.getItemDamage() + "-" + hashCode);
    }

    /**
     * Search a workOrder for the worker.
     */
    public abstract void searchWorkOrder();

    /**
     * Set the progress position of the builder.
     * @param blockPos the last blockPos.
     * @param stage the stage to set.
     */
    public void setProgressPos(final BlockPos blockPos, final StructureIterator.Stage stage)
    {
        this.progressPos = blockPos;
        this.progressStage = stage;
        if (this.progressCounter > COUNT_TO_STORE_POS || blockPos == null)
        {
            this.markDirty();
            this.progressCounter = 0;
        }
        else
        {
            this.progressCounter++;
        }
    }

    /**
     * Getter for the progress position.
     * @return the current progress and stage.
     */
    @Nullable
    public Tuple<BlockPos, StructureIterator.Stage> getProgress()
    {
        if (this.progressPos == null)
        {
            return null;
        }
        return new Tuple<>(this.progressPos, this.progressStage);
    }
}
