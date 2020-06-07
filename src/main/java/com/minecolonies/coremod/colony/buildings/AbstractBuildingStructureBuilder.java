package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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
    private BuildingStructureHandler.Stage progressStage;

    /**
     * Contains all resources needed for a certain build.
     */
    private Map<String, BuildingBuilderResource> neededResources = new LinkedHashMap<>();

    /**
     * The progress counter of the builder.
     */
    private int progressCounter = 0;

    /**
     * all the fluids to be removed in fluids_remove.
     */
    private Map<Integer, List<BlockPos>> fluidsToRemove = new LinkedHashMap<>();

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public AbstractBuildingStructureBuilder(final IColony c, final BlockPos l)
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
    public Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        for (final BuildingBuilderResource stack : neededResources.values())
        {
            toKeep.put(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, true), new net.minecraft.util.Tuple<>(stack.getAmount(), true));
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
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT neededResTagList = compound.getList(TAG_RESOURCE_LIST, net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.size(); ++i)
        {
            final CompoundNBT neededRes = neededResTagList.getCompound(i);
            final ItemStack stack = ItemStack.read(neededRes);

            if (!stack.isEmpty())
            {
                final BuildingBuilderResource resource = new BuildingBuilderResource(stack, ItemStackUtils.getSize(stack));
                final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
                neededResources.put(stack.getTranslationKey() + "-" + hashCode, resource);
            }
        }

        if (compound.contains(TAG_PROGRESS_POS))
        {
            progressPos = BlockPosUtil.read(compound, TAG_PROGRESS_POS);
            progressStage = BuildingStructureHandler.Stage.values()[compound.getInt(TAG_PROGRESS_STAGE)];
        }

        if (compound.contains(TAG_FLUIDS_REMOVE))
        {
            fluidsToRemove.clear();
            ListNBT fluidsToRemove = (ListNBT) compound.get(TAG_FLUIDS_REMOVE);
            fluidsToRemove.forEach(fluidsRemove -> {
            	int y = ((CompoundNBT) fluidsRemove).getInt(TAG_FLUIDS_REMOVE_Y);
                ListNBT positions = (ListNBT) ((CompoundNBT) fluidsRemove).get(TAG_FLUIDS_REMOVE_POSITIONS);
                final List<BlockPos> fluids = new ArrayList<BlockPos>();
                for (int i = 0; i < positions.size(); i++)
                {
                	fluids.add(BlockPosUtil.readFromListNBT(positions, i));
                }
                this.fluidsToRemove.put(y, fluids);
            });
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final ListNBT neededResTagList = new ListNBT();
        for (@NotNull final BuildingBuilderResource resource : neededResources.values())
        {
            @NotNull final CompoundNBT neededRes = new CompoundNBT();
            final ItemStack itemStack = new ItemStack(resource.getItem(), resource.getAmount());
            itemStack.setTag(resource.getItemStack().getTag());
            itemStack.write(neededRes);

            neededResTagList.add(neededRes);
        }

        compound.put(TAG_RESOURCE_LIST, neededResTagList);
        if (progressPos != null)
        {
            BlockPosUtil.write(compound, TAG_PROGRESS_POS, progressPos);
            compound.putInt(TAG_PROGRESS_STAGE, progressStage.ordinal());
        }

        final ListNBT fluidsToRemove = new ListNBT();
        this.fluidsToRemove.forEach((y, fluids) -> {
        	final CompoundNBT fluidsRemove = new CompoundNBT();
            final ListNBT positions = new ListNBT();
            fluids.forEach(fluid -> BlockPosUtil.writeToListNBT(positions, fluid));
            fluidsRemove.put(TAG_FLUIDS_REMOVE_POSITIONS, positions);
            fluidsRemove.putInt(TAG_FLUIDS_REMOVE_Y, y);
            fluidsToRemove.add(fluidsRemove);
        });
        compound.put(TAG_FLUIDS_REMOVE, fluidsToRemove);

        return compound;
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);

        updateAvailableResources();
        buf.writeInt(neededResources.size());
        double qty = 0;
        for (@NotNull final BuildingBuilderResource resource : neededResources.values())
        {
            buf.writeItemStack(resource.getItemStack());
            buf.writeInt(resource.getAvailable());
            buf.writeInt(resource.getAmount());
            qty += resource.getAmount();
        }

        final ICitizenData data = this.getMainCitizen();
        if(data != null && data.getJob() instanceof AbstractJobStructure)
        {
            final AbstractJobStructure<?, ?> structureBuilderJob = (AbstractJobStructure<?, ?>) data.getJob();
            final WorkOrderBuildDecoration workOrderBuildDecoration = structureBuilderJob.getWorkOrder();
            if(workOrderBuildDecoration != null)
            {
                final BlockPos pos = workOrderBuildDecoration.getBuildingLocation();
                final String name =
                        workOrderBuildDecoration instanceof WorkOrderBuild ? ((WorkOrderBuild) workOrderBuildDecoration).getUpgradeName() : workOrderBuildDecoration.getName();
                buf.writeString(name);

                final String desc;
                if(pos.equals(getPosition()))
                {
                    desc = "here";
                }
                else
                {
                    final BlockPos relativePos = getPosition().subtract(pos);
                    final Direction facingX = Direction.getFacingFromVector(relativePos.getX(), 0, 0);
                    final Direction facingZ = Direction.getFacingFromVector(0, 0, relativePos.getZ());
                    desc = relativePos.getX() + " " + facingX + " " + relativePos.getZ() + " " + facingZ;
                }

                buf.writeString(desc);
                buf.writeDouble(workOrderBuildDecoration.getAmountOfRes() == 0 ? 0 : qty/workOrderBuildDecoration.getAmountOfRes());
            }
            else
            {
                buf.writeString("-");
                buf.writeString("");
                buf.writeDouble(0.0);
            }
        }
        else
        {
            buf.writeString("-");
            buf.writeString("");
            buf.writeDouble(0.0);
        }
        buf.writeString((getMainCitizen() == null || colony.getCitizenManager().getCitizen(getMainCitizen().getId()) == null) ? "" : getMainCitizen().getName());

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

                    resource.addAvailable(InventoryUtils.getItemCountInItemHandler(structureBuilderInventory,
                            stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resource.getItemStack(), true, true)));
                }

                if (getTileEntity() != null)
                {
                    resource.addAvailable(InventoryUtils.getItemCountInItemHandler(this.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
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
        final int hashCode = res.hasTag() ? res.getTag().hashCode() : 0;
        BuildingBuilderResource resource = this.neededResources.get(res.getTranslationKey() + "-" + hashCode);
        if (resource == null)
        {
            resource = new BuildingBuilderResource(res, amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        this.neededResources.put(res.getTranslationKey() + "-" + hashCode, resource);
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
        final int hashCode = res.hasTag() ? res.getTag().hashCode() : 0;
        int preAmount = 0;
        final String name = res.getTranslationKey() + "-" + hashCode;
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
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        return neededResources.containsKey(stack.getTranslationKey() + "-" + hashCode);
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
    public void setProgressPos(final BlockPos blockPos, final BuildingStructureHandler.Stage stage)
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
    public Tuple<BlockPos, BuildingStructureHandler.Stage> getProgress()
    {
        if (this.progressPos == null)
        {
            return null;
        }
        return new Tuple<>(this.progressPos, this.progressStage);
    }

    /**
     * Getter for the blocks to be removed in fluids_remove.
     * @return the blocks to be removed in fluids_remove.
     */
    public Map<Integer, List<BlockPos>> getFluidsToRemove()
    {
        return fluidsToRemove;
    }
}
