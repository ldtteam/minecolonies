package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuilderBucket;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * The structureBuilder building.
 */
public class BuildingResourcesModule extends AbstractBuildingModule implements IPersistentModule
{
    /**
     * Contains all resources needed for a certain build.
     */
    private Map<String, BuildingBuilderResource> neededResources = new LinkedHashMap<>();

    /**
     * The different possible buckets.
     */
    private Deque<BuilderBucket> buckets = new ArrayDeque<>();

    /**
     * Total amount of stages.
     */
    private int totalStages = 0;
    private int currentStage = 0;

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        currentStage = compound.getInt(TAG_CURR_STAGE);
        totalStages = compound.getInt(TAG_TOTAL_STAGES);
    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {
        compound.putInt(TAG_TOTAL_STAGES, totalStages);
        compound.putInt(TAG_CURR_STAGE, currentStage);
    }

    /**
     * Method to serialize data to send it to the view.
     *
     * @param buf the used ByteBuffer.
     */
    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
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

        final ICitizenData data = building.getMainCitizen();
        if (data != null && data.getJob() instanceof AbstractJobStructure)
        {
            final AbstractJobStructure<?, ?> structureBuilderJob = (AbstractJobStructure<?, ?>) data.getJob();
            final WorkOrderBuildDecoration workOrderBuildDecoration = structureBuilderJob.getWorkOrder();
            if (workOrderBuildDecoration != null)
            {
                final String name = workOrderBuildDecoration instanceof WorkOrderBuild ? ((WorkOrderBuild) workOrderBuildDecoration).getUpgradeName() : workOrderBuildDecoration.getName();
                buf.writeString(name);

                buf.writeDouble(workOrderBuildDecoration.getAmountOfRes() == 0 ? 0 : qty / workOrderBuildDecoration.getAmountOfRes());
                buf.writeInt(totalStages);
                buf.writeInt(currentStage);
                return;
            }
        }

        buf.writeString("");
        buf.writeDouble(0.0);
        buf.writeInt(0);
        buf.writeInt(0);
    }

    /**
     * Update the available resources.
     * <p>
     * which are needed for the build and in the structureBuilder's chest or inventory
     */
    private void updateAvailableResources()
    {
        building.getMainCitizenEntity().ifPresent(structureBuilder -> {
            final InventoryCitizen structureBuilderInventory = building.getMainCitizen().getInventory();
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

                if (building.getTileEntity() != null)
                {
                    resource.addAvailable(InventoryUtils.getItemCountInItemHandler(building.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null),
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
        return (buckets.isEmpty() || ((AbstractBuildingStructureBuilder) building).getProgress() == null || ((AbstractBuildingStructureBuilder) building).getProgress().getB() == BuildingStructureHandler.Stage.CLEAR) ? null : buckets.getFirst();
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
        final int max = building.getMainCitizen().getInventory().getSlots() - 9;

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

        final ImmutableList<IRequest<? extends Stack>> list = building.getOpenRequestsOfType(worker, TypeToken.of(Stack.class));
        for (final Map.Entry<String, Integer> entry : requiredResources.getResourceMap().entrySet())
        {
            final ItemStorage itemStack = neededResources.get(entry.getKey());
            if (itemStack == null)
            {
                continue;
            }

            boolean hasOpenRequest = false;
            int count = InventoryUtils.getItemCountInItemHandler(building.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null),
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

                    worker.createRequestAsync(new Stack(itemStack.getItemStack(), requestCount * ((AbstractBuildingStructureBuilder) building).getResourceBatchMultiplier(), 1));
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
