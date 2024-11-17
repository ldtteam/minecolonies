package com.minecolonies.core.colony.buildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.BuildingResourcesModule;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.utils.BuilderBucket;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The structureBuilder building.
 */
public abstract class AbstractBuildingStructureBuilder extends AbstractBuilding
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
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory, final JobEntry jobEntry)
    {
        if (inventory)
        {
            final int hashCode = stack.getComponentsPatch().hashCode();
            final String key = stack.getDescriptionId() + "-" + hashCode;
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
            if (checkIfShouldKeepEquipment(ModEquipmentTypes.pickaxe.get(), stack, localAlreadyKept)
                  || checkIfShouldKeepEquipment(ModEquipmentTypes.shovel.get(), stack, localAlreadyKept)
                  || checkIfShouldKeepEquipment(ModEquipmentTypes.axe.get(), stack, localAlreadyKept)
                  || checkIfShouldKeepEquipment(ModEquipmentTypes.hoe.get(), stack, localAlreadyKept))
            {
                localAlreadyKept.add(new ItemStorage(stack, 1, true));
                return 0;
            }
        }
        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory, jobEntry);
    }

    /**
     * Check if certain equipment should be kept or dumped.
     *
     * @param type             the type of the equipment.
     * @param stack            the stack to check.
     * @param localAlreadyKept the already kept stacks.
     * @return true if should keep.
     */
    private boolean checkIfShouldKeepEquipment(final EquipmentTypeEntry type, final ItemStack stack, final List<ItemStorage> localAlreadyKept)
    {
        if (ItemStackUtils.hasEquipmentLevel(stack, type, TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()))
        {
            for (final ItemStorage storage : localAlreadyKept)
            {
                if (type.getMiningLevel(stack) <= type.getMiningLevel(storage.getItemStack()))
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

        for (final BuildingBuilderResource stack : getModule(BuildingModules.BUILDING_RESOURCES).getNeededResources().values())
        {
            toKeep.put(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), itemstack),
              new net.minecraft.util.Tuple<>(stack.getAmount(), true));
        }

        return toKeep;
    }

    @Override
    public ItemStack forceTransferStack(final ItemStack stack, final Level world)
    {
        final ItemStack itemStack = super.forceTransferStack(stack, world);
        if (ItemStackUtils.isEmpty(itemStack))
        {
            this.markDirty();
        }

        return itemStack;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);
        if (compound.contains(TAG_PROGRESS_POS))
        {
            progressPos = BlockPosUtil.read(compound, TAG_PROGRESS_POS);
            progressStage = BuildingStructureHandler.Stage.values()[compound.getInt(TAG_PROGRESS_STAGE)];
        }

        if (compound.contains(TAG_FLUIDS_REMOVE))
        {
            fluidsToRemove.clear();
            ListTag fluidsToRemove = (ListTag) compound.get(TAG_FLUIDS_REMOVE);
            fluidsToRemove.forEach(fluidsRemove -> {
                int y = ((CompoundTag) fluidsRemove).getInt(TAG_FLUIDS_REMOVE_Y);
                ListTag positions = (ListTag) ((CompoundTag) fluidsRemove).get(TAG_FLUIDS_REMOVE_POSITIONS);
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
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = super.serializeNBT(provider);
        if (progressPos != null)
        {
            BlockPosUtil.write(compound, TAG_PROGRESS_POS, progressPos);
            compound.putInt(TAG_PROGRESS_STAGE, progressStage.ordinal());
        }

        final ListTag fluidsToRemove = new ListTag();
        this.fluidsToRemove.forEach((y, fluids) -> {
            final CompoundTag fluidsRemove = new CompoundTag();
            final ListTag positions = new ListTag();
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
    public void serializeToView(@NotNull final RegistryFriendlyByteBuf buf, final boolean fullSync)
    {
        super.serializeToView(buf, fullSync);

        final WorkerBuildingModule module = getFirstModuleOccurance(WorkerBuildingModule.class);
        buf.writeUtf(module.getFirstCitizen() != null ? module.getFirstCitizen().getName() : "");
    }

    /**
     * Get the needed resources for the current build.
     *
     * @return a new Hashmap.
     */
    public Map<String, BuildingBuilderResource> getNeededResources()
    {
        return getModule(BuildingModules.BUILDING_RESOURCES).getNeededResources();
    }

    /**
     * Get the needed resources for the current build.
     *
     * @return the bucket.
     */
    @Nullable
    public BuilderBucket getRequiredResources()
    {
        return getModule(BuildingModules.BUILDING_RESOURCES).getRequiredResources();
    }

    /**
     * Check if the resources are in the bucket.
     *
     * @param stack the stack to check.
     * @return true if so.
     */
    public boolean hasResourceInBucket(final ItemStack stack)
    {
        final int hashCode = stack.getComponentsPatch().hashCode();
        final String key = stack.getDescriptionId() + "-" + hashCode;
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
        getModule(BuildingModules.BUILDING_RESOURCES).addNeededResource(res, amount);
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
        getModule(BuildingModules.BUILDING_RESOURCES).reduceNeededResource(res, amount);
        this.markDirty();
    }

    /**
     * Resets the needed resources completely.
     */
    public void resetNeededResources()
    {
        getFirstModuleOccurance(BuildingResourcesModule.class).resetNeededResources();
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
       return getFirstModuleOccurance(BuildingResourcesModule.class).requiresResourceForBuilding(stack);
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
     * Batch size to request for resources, used by the Miner to get multiple nodes of supplies
     */
    public int getResourceBatchMultiplier()
    {
        return 1;
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
     */
    public void checkOrRequestBucket(@Nullable final BuilderBucket requiredResources, final ICitizenData worker)
    {
        getFirstModuleOccurance(BuildingResourcesModule.class).checkOrRequestBucket(requiredResources, worker);
    }

    /**
     * Go to the next stage.
     */
    public void nextStage()
    {
        getFirstModuleOccurance(BuildingResourcesModule.class).nextStage();
    }

    /**
     * Set the total number of stages.
     * @param total the total.
     */
    public void setTotalStages(final int total)
    {
        getFirstModuleOccurance(BuildingResourcesModule.class).setTotalStages(total);
    }

    /**
     * Return the next bucket to work on.
     *
     * @return the next bucket or a tuple with null inside if non available.
     */
    @Nullable
    public BuilderBucket getNextBucket()
    {
        return getFirstModuleOccurance(BuildingResourcesModule.class).getNextBucket();
    }
}
