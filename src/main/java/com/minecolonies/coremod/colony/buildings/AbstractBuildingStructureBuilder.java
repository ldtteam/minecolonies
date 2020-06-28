package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizen;
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
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.citizen.healer.Patient;
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
     * The different possible buckets.
     */
    private Deque<Tuple<Map<String, Integer>, Integer>> buckets = new ArrayDeque<>();

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
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory)
    {
        if (inventory)
        {
            final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
            final String key = stack.getTranslationKey() + "-" + hashCode;
            if (getRequiredResources().getA().containsKey(key))
            {
                final int qtyToKeep = getRequiredResources().getA().get(key);
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
                            if (qtyToKeep >= kept +  stack.getCount())
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
            return stack.getCount();
        }
        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory);
    }

    @Override
    public Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, net.minecraft.util.Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        for (final BuildingBuilderResource stack : neededResources.values())
        {
            toKeep.put(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack, true, false), new net.minecraft.util.Tuple<>(stack.getAmount(), true));
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
     * Get the needed resources for the current build.
     *
     * @return the bucket.
     */
    public Tuple<Map<String, Integer>, Integer> getRequiredResources()
    {
        return buckets.getFirst();
    }

    /**
     * Get the resource from the identifier.
     * @param res the resource to get.
     * @return the resource.
     */
    public BuildingBuilderResource getResourceFromIdentifier(final String res)
    {
        return neededResources.get(res);
    }

    /**
     * Check if the resources are in the bucket.
     * @param stack the stack to check.
     * @return true if so.
     */
    public boolean hasResourceInBucket(final ItemStack stack)
    {
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        final String key = stack.getTranslationKey() + "-" + hashCode;
        return getRequiredResources() != null && getRequiredResources().getA().containsKey(key);
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

        final Tuple<Map<String, Integer>, Integer> last = buckets.isEmpty() ? null : buckets.removeLast();

        final int stacks = (int) Math.ceil((double) amount / res.getMaxStackSize());
        final int max = getMainCitizen().getInventory().getSlots() - 9;

        if (last == null || last.getB() >= max || last.getB() + stacks >= max)
        {
            if (last != null)
            {
                buckets.add(last);
            }
            final Map<String, Integer> map = new HashMap<>();
            map.put(key, amount);
            final Tuple<Map<String, Integer>, Integer> newTuple = new Tuple<>(map, stacks);
            buckets.add(newTuple);
        }
        else
        {
            int currentQty = last.getA().getOrDefault(key, 0);
            final int currentStacks = (int) Math.ceil((double) currentQty / res.getMaxStackSize());
            final int newStacks = (int) Math.ceil((double) ( currentQty + amount ) / res.getMaxStackSize());
            final Map<String, Integer> map = last.getA();
            map.put(key, currentQty + amount);
            final Tuple<Map<String, Integer>, Integer> newTuple = new Tuple<>(map, last.getB() + newStacks - currentStacks);
            buckets.add(newTuple);
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

        final Tuple<Map<String, Integer>, Integer> last = buckets.isEmpty() ? null : getRequiredResources();

        if (last != null)
        {
            final Map<String, Integer> map = last.getA();
            if (map.containsKey(name))
            {
                int qty = map.get(name) - amount;
                if (qty > 0)
                {
                    map.put(name, map.get(name) - amount);
                }
                else
                {
                     map.remove(name);
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


    //todo we need to make sure that when the builder actually creates a real request, that those don't cause problems with eachother.
    //todo we need to check what happens with partial deliveries or non-deliveries
    //todo it seems that we request things completely that the builder already finished, also we seem to request from the current bucket AGAIN. (it seems only the count is off).
    /**
     * Check or request if the contents of a specific batch are in the inventory of the building.
     * This ignores the worker inventory (that is remaining stuff from previous rounds, or already belongs to another bucket)
     * @param requiredResources the bucket to check and request.
     */
    public void checkOrRequestBucket(final Map<String, Integer> requiredResources, final ICitizenData worker)
    {
        if (requiredResources == null)
        {
            return;
        }

        final ImmutableList<IRequest<? extends Stack>> list = getOpenRequestsOfType(worker, TypeToken.of(Stack.class));
        for (final Map.Entry<String, Integer> entry : requiredResources.entrySet())
        {
            final ItemStorage itemStack = neededResources.get(entry.getKey());
            if (itemStack == null)
            {
                continue;
            }

            boolean hasOpenRequest = false;
            if (InventoryUtils.getItemCountInItemHandler(getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null), stack -> stack.isItemEqual(itemStack.getItemStack())) < entry.getValue())
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

                worker.createRequestAsync(new Stack(itemStack.getItemStack(), entry.getValue(), entry.getValue()));
            }
        }
    }

    /**
     * Return the next bucket to work on.
     * @return the next bucket or a tuple with null inside if non available.
     */
    public Tuple<Map<String, Integer>, Integer> getNextBucket()
    {
        final Iterator<Tuple<Map<String, Integer>, Integer>> iterator = buckets.iterator();
        if (iterator.hasNext())
        {
            iterator.next();
        }

        if (iterator.hasNext())
        {
            return iterator.next();
        }
        return new Tuple<>(null, 0);
    }
}
