package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.utils.BuilderBucket;
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
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * The structureBuilder building.
 */
public abstract class AbstractBuildingStructureBuilder extends AbstractBuildingWorker
{
    /**
     * The maximum upgrade of the building.
     */
    public static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Progress amount to mark building dirty.
     */
    private static final int COUNT_TO_STORE_POS = 50;

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
     * The different possible buckets.
     */
    private Deque<BuilderBucket> buckets = new ArrayDeque<>();

    /**
     * The progress counter of the builder.
     */
    private int progressCounter = 0;

    /**
     * all the fluids to be removed in fluids_remove.
     */
    private Map<Integer, List<BlockPos>> fluidsToRemove = new LinkedHashMap<>();

    /**
     * Total amount of stages.
     */
    private int totalStages = 0;
    private int currentStage = 0;

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

     /**
     * Batch size to request for resources, used by the Miner to get multiple nodes of supplies
     */
    public int getResourceBatchMultiplier()
    {
        return 1;
    } 


    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory)
    {
        if (inventory)
        {
            final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
            final String key = stack.getTranslationKey() + "-" + hashCode;
            if (getRequiredResources() != null && getRequiredResources().getResourceMap().containsKey(key))
            {
                final int qtyToKeep = getRequiredResources().getResourceMap().get(key);
                if (localAlreadyKept.contains(new ItemStorage(stack)))
                {
                    for (final ItemStorage storage : localAlreadyKept)
                    {
                        if (storage.equals(new ItemStorage(stack)))
                        {
                            if (storage.getAmount() >= qtyToKeep)
                            {
                                return stack.getCount();
                            }
                            final int kept = storage.getAmount();
                            if (qtyToKeep >= kept + stack.getCount())
                            {
                                storage.setAmount(kept + stack.getCount());
                                return 0;
                            }
                            else
                            {
                                storage.setAmount(qtyToKeep);
                                return qtyToKeep - kept - stack.getCount();
                            }
                        }
                    }
                }
                else
                {
                    if (qtyToKeep >= stack.getCount())
                    {
                        localAlreadyKept.add(new ItemStorage(stack));
                        return 0;
                    }
                    else
                    {
                        localAlreadyKept.add(new ItemStorage(stack, qtyToKeep, false));
                        return stack.getCount() - qtyToKeep;
                    }
                }
            }
            if (checkIfShouldKeepTool(ToolType.PICKAXE, stack, localAlreadyKept)
                  || checkIfShouldKeepTool(ToolType.SHOVEL, stack, localAlreadyKept)
                  || checkIfShouldKeepTool(ToolType.AXE, stack, localAlreadyKept)
                  || checkIfShouldKeepTool(ToolType.HOE, stack, localAlreadyKept))
            {
                localAlreadyKept.add(new ItemStorage(stack, 1, true));
                return 0;
            }
            return stack.getCount();
        }
        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory);
    }

    /**
     * Check if a certain tool should be kept or dumped.
     *
     * @param type             the type of the tool.
     * @param stack            the stack to check.
     * @param localAlreadyKept the already kept stacks.
     * @return true if should keep.
     */
    private boolean checkIfShouldKeepTool(final ToolType type, final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        if (ItemStackUtils.hasToolLevel(stack, type, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()))
        {
            for (final ItemStorage storage : localAlreadyKept)
            {
                if (ItemStackUtils.getMiningLevel(stack, type) <= ItemStackUtils.getMiningLevel(storage.getItemStack(), type))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        for (final BuildingBuilderResource stack : neededResources.values())
        {
            toKeep.put(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, false),
              new net.minecraft.util.Tuple<>(stack.getAmount(), true));
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


        currentStage = compound.getInt(TAG_CURR_STAGE);
        totalStages = compound.getInt(TAG_TOTAL_STAGES);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
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
        compound.putInt(TAG_TOTAL_STAGES, totalStages);
        compound.putInt(TAG_CURR_STAGE, currentStage);

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
        if (data != null && data.getJob() instanceof AbstractJobStructure)
        {
            final AbstractJobStructure<?, ?> structureBuilderJob = (AbstractJobStructure<?, ?>) data.getJob();
            final WorkOrderBuildDecoration workOrderBuildDecoration = structureBuilderJob.getWorkOrder();
            if (workOrderBuildDecoration != null)
            {
                final BlockPos pos = workOrderBuildDecoration.getBuildingLocation();
                final String name =
                  workOrderBuildDecoration instanceof WorkOrderBuild ? ((WorkOrderBuild) workOrderBuildDecoration).getUpgradeName() : workOrderBuildDecoration.getName();
                buf.writeString(name);

                final String desc;
                if (pos.equals(getPosition()))
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
                buf.writeDouble(workOrderBuildDecoration.getAmountOfRes() == 0 ? 0 : qty / workOrderBuildDecoration.getAmountOfRes());
                buf.writeInt(totalStages);
                buf.writeInt(currentStage);
            }
            else
            {
                buf.writeString("-");
                buf.writeString("");
                buf.writeDouble(0.0);
                buf.writeInt(0);
                buf.writeInt(0);
            }
        }
        else
        {
            buf.writeString("-");
            buf.writeString("");
            buf.writeDouble(0.0);
            buf.writeInt(0);
            buf.writeInt(0);
        }
        buf.writeString((getMainCitizen() == null || colony.getCitizenManager().getCivilian(getMainCitizen().getId()) == null) ? "" : getMainCitizen().getName());
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
     * Get the needed resources for the current build.
     *
     * @return the bucket.
     */
    @Nullable
    public BuilderBucket getRequiredResources()
    {
        return (buckets.isEmpty() || currentStage == 0) ? null : buckets.getFirst();
    }

    /**
     * Get the resource from the identifier.
     *
     * @param res the resource to get.
     * @return the resource.
     */
    public BuildingBuilderResource getResourceFromIdentifier(final String res)
    {
        return neededResources.get(res);
    }

    /**
     * Check if the resources are in the bucket.
     *
     * @param stack the stack to check.
     * @return true if so.
     */
    public boolean hasResourceInBucket(final ItemStack stack)
    {
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        final String key = stack.getTranslationKey() + "-" + hashCode;
        return getRequiredResources() != null && getRequiredResources().getResourceMap().containsKey(key);
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
        final String key = res.getTranslationKey() + "-" + hashCode;
        BuildingBuilderResource resource = this.neededResources.get(key);
        if (resource == null)
        {
            resource = new BuildingBuilderResource(res, amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        this.neededResources.put(key, resource);

        BuilderBucket last = buckets.isEmpty() ? null : buckets.removeLast();

        final int stacks = (int) Math.ceil((double) amount / res.getMaxStackSize());
        final int max = getMainCitizen().getInventory().getSlots() - 9;

        if (last == null || last.getTotalStacks() >= max || last.getTotalStacks() + stacks >= max)
        {
            if (last != null)
            {
                buckets.add(last);
            }
            last = new BuilderBucket();

            last.setTotalStacks(stacks);
            last.addOrAdjustResource(key, amount);
            buckets.add(last);
        }
        else
        {
            int currentQty = last.getResourceMap().getOrDefault(key, 0);
            final int currentStacks = (int) Math.ceil((double) currentQty / res.getMaxStackSize());
            final int newStacks = (int) Math.ceil((double) (currentQty + amount) / res.getMaxStackSize());
            final Map<String, Integer> map = last.getResourceMap();
            last.setTotalStacks(last.getTotalStacks() + newStacks - currentStacks);
            last.addOrAdjustResource(key, currentQty + amount);
            buckets.add(last);
        }

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
        final String name = res.getTranslationKey() + "-" + hashCode;

        final BuilderBucket last = buckets.isEmpty() ? null : getRequiredResources();

        if (last != null)
        {
            final Map<String, Integer> map = last.getResourceMap();
            if (map.containsKey(name))
            {
                int qty = map.get(name) - amount;
                if (qty > 0)
                {
                    last.addOrAdjustResource(name, map.get(name) - amount);
                }
                else
                {
                    last.removeResources(name);
                }
            }

            if (map.isEmpty())
            {
                buckets.remove();
            }
        }

        int preAmount = 0;
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
        buckets.clear();
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
     *
     * @param blockPos the last blockPos.
     * @param stage    the stage to set.
     */
    public void setProgressPos(final BlockPos blockPos, final BuildingStructureHandler.Stage stage)
    {
        this.progressPos = blockPos;
        if (this.progressCounter > COUNT_TO_STORE_POS || blockPos == null || stage != progressStage)
        {
            this.markDirty();
            this.progressCounter = 0;
        }
        else
        {
            this.progressCounter++;
        }
        this.progressStage = stage;
    }

    /**
     * Getter for the progress position.
     *
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
     *
     * @return the blocks to be removed in fluids_remove.
     */
    public Map<Integer, List<BlockPos>> getFluidsToRemove()
    {
        return fluidsToRemove;
    }

    /**
     * Check or request if the contents of a specific batch are in the inventory of the building. This ignores the worker inventory (that is remaining stuff from previous rounds,
     * or already belongs to another bucket)
     *
     * @param requiredResources the bucket to check and request.
     * @param worker            the worker.
     * @param workerInv         if the worker inv should be checked too.
     */
    public void checkOrRequestBucket(@Nullable final BuilderBucket requiredResources, final ICitizenData worker, final boolean workerInv)
    {
        if (requiredResources == null)
        {
            return;
        }

        final ImmutableList<IRequest<? extends Stack>> list = getOpenRequestsOfType(worker, TypeToken.of(Stack.class));
        for (final Map.Entry<String, Integer> entry : requiredResources.getResourceMap().entrySet())
        {
            final ItemStorage itemStack = neededResources.get(entry.getKey());
            if (itemStack == null)
            {
                continue;
            }

            boolean hasOpenRequest = false;
            int count = InventoryUtils.getItemCountInItemHandler(getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null),
              stack -> stack.isItemEqual(itemStack.getItemStack()));

            int totalAmount = neededResources.containsKey(entry.getKey()) ? neededResources.get(entry.getKey()).getAmount() : 0;
            int workerInvCount = InventoryUtils.getItemCountInItemHandler(worker.getInventory(), stack -> stack.isItemEqual(itemStack.getItemStack()));
            if ((workerInv && (count + workerInvCount) < entry.getValue())
                  || (count < entry.getValue() && (count + workerInvCount) < totalAmount))
            {
                int requestCount = entry.getValue() - count - (workerInv ? workerInvCount : 0);
                if (requestCount > 0)
                {
                    for (final IRequest<? extends Stack> request : list)
                    {
                        if (request.getRequest().getStack().isItemEqual(itemStack.getItemStack()))
                        {
                            hasOpenRequest = true;
                            break;
                        }
                    }
                    if (hasOpenRequest)
                    {
                        break;
                    }

                    worker.createRequestAsync(new Stack(itemStack.getItemStack(), requestCount * getResourceBatchMultiplier(), 1));
                }
            }
        }
    }

    /**
     * Go to the next stage.
     */
    public void nextStage()
    {
        if (this.currentStage + 1 > totalStages)
        {
            totalStages++;
        }
        this.currentStage++;
    }

    /**
     * Set the total number of stages.
     * @param total the total.
     */
    public void setTotalStages(final int total)
    {
        this.totalStages = total;
        this.currentStage = 0;
    }

    /**
     * Return the next bucket to work on.
     *
     * @return the next bucket or a tuple with null inside if non available.
     */
    @Nullable
    public BuilderBucket getNextBucket()
    {
        final Iterator<BuilderBucket> iterator = buckets.iterator();
        if (iterator.hasNext())
        {
            iterator.next();
        }

        if (iterator.hasNext())
        {
            return iterator.next();
        }
        return null;
    }
}
