package com.minecolonies.coremod.entity.ai.basic;

import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.StructurePlacementUtils;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.util.StructureIterator;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public abstract class AbstractEntityAIStructureWithWorkOrder<J extends AbstractJobStructure> extends AbstractEntityAIStructure<J>
{
    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public AbstractEntityAIStructureWithWorkOrder(@NotNull final J job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return AbstractBuildingStructureBuilder.class;
    }

    @Override
    public void storeProgressPos(final BlockPos blockPos, final StructureIterator.Stage stage)
    {
        getOwnBuilding(AbstractBuildingStructureBuilder.class).setProgressPos(blockPos, stage);
    }

    @Override
    public Tuple<BlockPos, StructureIterator.Stage> getProgressPos()
    {
        return getOwnBuilding(AbstractBuildingStructureBuilder.class).getProgress();
    }

    /**
     * Takes the existing workorder, loads the structure and tests the worker order if it is valid.
     */
    public void initiate()
    {
        if (!job.hasStructure())
        {
            loadStructure();
            final WorkOrderBuildDecoration wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                  String.format("Worker (%d:%d) ERROR - Starting and missing work order(%d)",
                    worker.getCitizenColonyHandler().getColony().getID(),
                    worker.getCitizenData().getId(), job.getWorkOrderId()));
                return;
            }

            if (wo instanceof WorkOrderBuildBuilding)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Worker (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getCitizenColonyHandler().getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return;
                }

                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, job.getStructure().getBluePrint().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
            else if(!(wo instanceof WorkOrderBuildMiner))
            {
                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, wo.getName());
            }
        }
    }

    /**
     * Load the struction into the AI.
     */
    private void loadStructure()
    {
        final WorkOrderBuildDecoration workOrder = job.getWorkOrder();

        if (workOrder == null)
        {
            return;
        }

        final BlockPos pos = workOrder.getBuildingLocation();
        if (workOrder instanceof WorkOrderBuildBuilding && worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(pos) == null)
        {
            Log.getLogger().warn("AbstractBuilding does not exist - removing build request");
            worker.getCitizenColonyHandler().getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        final int tempRotation = workOrder.getRotation(world);
        final boolean removal = workOrder instanceof WorkOrderBuildRemoval;

        super.loadStructure(workOrder.getStructureName(), tempRotation, pos, workOrder.isMirrored(), removal);
        workOrder.setCleared(false);
        workOrder.setRequested(false);

        //We need to deal with materials
        requestMaterialsState();
        if (getProgressPos() != null)
        {
            job.getStructure().setLocalPosition(getProgressPos().getA());
        }
    }

    /**
     * State for material requesting.
     */
    private void requestMaterialsState()
    {
        if (MineColonies.getConfig().getCommon().builderInfiniteResources.get() || job.getWorkOrder().isRequested() || job.getWorkOrder() instanceof WorkOrderBuildRemoval)
        {
            return;
        }
        requestMaterials();

        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding(AbstractBuildingStructureBuilder.class);
        job.getWorkOrder().setRequested(true);

        if (job.getWorkOrder().getAmountOfRes() == 0)
        {
            job.getWorkOrder().setAmountOfRes(buildingWorker.getNeededResources().values().stream()
                                                .mapToInt(ItemStorage::getAmount).sum());
        }
    }

    /**
     * Iterates through all the required resources and stores them in the building.
     * Suppressing Sonar Rule Squid:S135
     * The rule thinks we should have less continue and breaks.
     * But in this case the rule does not apply because code would become unreadable and uneffective without.
     */
    private void requestMaterials()
    {
        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding(AbstractBuildingStructureBuilder.class);
        buildingWorker.resetNeededResources();

        for (final BlockInfo blockInfo : job.getStructure().getBluePrint().getBlockInfoAsList())
        {
            if (blockInfo == null)
            {
                continue;
            }
            final BlockPos worldPos = blockInfo.getPos().add(job.getStructure().getOffsetPosition());

            @Nullable BlockState blockState = blockInfo.getState();
            @Nullable Block block = blockState.getBlock();

            if (StructurePlacementUtils.isStructureBlockEqualWorldBlock(world, worldPos, blockState)
                  || (blockState.getBlock() instanceof BedBlock && blockState.get(BedBlock.PART).equals(BedPart.FOOT))
                  || (blockState.getBlock() instanceof DoorBlock && blockState.get(DoorBlock.HALF).equals(DoubleBlockHalf.UPPER))
                  || blockState.getBlock() == Blocks.AIR)
            {
                continue;
            }

            @Nullable Block worldBlock = world.getBlockState(worldPos).getBlock();
            for (final IPlacementHandler handler : PlacementHandlers.handlers)
            {
                if (handler.canHandle(world, worldPos, blockState))
                {
                    for (final ItemStack stack : handler.getRequiredItems(world, worldPos, blockState, blockInfo.getTileEntityData(), false))
                    {
                        if (block != Blocks.AIR
                              && worldBlock != Blocks.BEDROCK
                              && !(worldBlock instanceof AbstractBlockHut)
                              && !isBlockFree(block))
                        {
                            buildingWorker.addNeededResource(stack, stack.getCount());
                        }
                    }
                    break;
                }
            }
        }

        for (final CompoundNBT entityInfo : job.getStructure().getEntityData())
        {
            if (entityInfo != null)
            {
                for (final ItemStorage stack : ItemStackUtils.getListOfStackForEntityInfo(entityInfo, world, worker))
                {
                    if (!ItemStackUtils.isEmpty(stack.getItemStack()))
                    {
                        buildingWorker.addNeededResource(stack.getItemStack(), 1);
                    }
                }
            }
        }
    }

    /**
     * Add blocks to the builder building if he needs it.
     *
     * @param building   the building.
     * @param blockState the block to add.
     * @param blockInfo the complete blockinfo.
     */
    private void requestBlockToBuildingIfRequired(final AbstractBuildingStructureBuilder building, final BlockState blockState, final BlockInfo blockInfo)
    {
        if (blockInfo.getTileEntityData() != null)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity());

            for (final ItemStack stack : itemList)
            {
                building.addNeededResource(stack, stack.getCount());
            }
        }

        if (!ChiselAndBitsCheck.isChiselAndBitsBlock(blockState)
              && !(blockState.getBlock() instanceof BedBlock && blockState.get(BedBlock.PART) == BedPart.FOOT)
              && !blockState.getBlock().isIn(BlockTags.BANNERS))
        {
            building.addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
        }
    }

    @Override
    public void registerBlockAsNeeded(final ItemStack stack)
    {
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        if (getOwnBuilding(AbstractBuildingStructureBuilder.class)
              .getNeededResources()
              .get(stack.getTranslationKey()
                     + "-" + hashCode) == null)
        {
            getOwnBuilding(AbstractBuildingStructureBuilder.class).addNeededResource(stack, 1);
        }
    }

    @Override
    public int getTotalRequiredAmount(final ItemStack deliveredItemStack)
    {
        if (ItemStackUtils.isEmpty(deliveredItemStack))
        {
            return 0;
        }
        final int hashCode = deliveredItemStack.hasTag() ? deliveredItemStack.getTag().hashCode() : 0;
        final BuildingBuilderResource resource
                = getOwnBuilding(AbstractBuildingStructureBuilder.class)
                .getNeededResources()
                .get(deliveredItemStack.getTranslationKey()
                        + "-" + hashCode);
        if (resource != null)
        {
            return resource.getAmount();
        }

        return super.getTotalRequiredAmount(deliveredItemStack);
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        if (job.getStructure() == null && job.hasWorkOrder())
        {
            //fix for bad structures
            job.complete();
        }

        if (job.getStructure() == null)
        {
            return;
        }

        final String structureName = job.getStructure().getBluePrint().getName();
        final WorkOrderBuildDecoration wo = job.getWorkOrder();

        if (wo instanceof WorkOrderBuildBuilding)
        {
            worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDCOMPLETE, structureName);
        }

        if (wo == null)
        {
            Log.getLogger().error(String.format("Worker (%d:%d) ERROR - Finished, but missing work order(%d)",
              worker.getCitizenColonyHandler().getColony().getID(),
              worker.getCitizenData().getId(),
              job.getWorkOrderId()));
        }
        else
        {
            job.complete();
            final WorkOrderBuildBuilding woh = (wo instanceof WorkOrderBuildBuilding) ? (WorkOrderBuildBuilding) wo : null;
            if (woh != null)
            {
                final IBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                            worker.getCitizenColonyHandler().getColony().getID(),
                            worker.getCitizenData().getId(),
                            woh.getBuildingLocation()));
                }
                else
                {
                    building.setBuildingLevel(woh.getUpgradeLevel());
                }
            }
        }
        getOwnBuilding(AbstractBuildingStructureBuilder.class).resetNeededResources();
        resetTask();
    }

    @Override
    public List<ItemStack> getItemsFromTileEntity()
    {
        if (job.getStructure() != null && job.getStructure().getBlockInfo() != null && job.getStructure().getBlockInfo().getTileEntityData() != null)
        {
            return com.ldtteam.structurize.api.util.ItemStackUtils.getItemStacksOfTileEntity(job.getStructure().getBlockInfo().getTileEntityData(), world);
        }
        return Collections.emptyList();
    }

    @Override
    public void reduceNeededResources(final ItemStack stack)
    {
        getOwnBuilding(AbstractBuildingStructureBuilder.class).reduceNeededResource(stack, 1);
    }

    @Override
    protected boolean checkIfCanceled()
    {
        if (job.getWorkOrder() == null && job.getStructure() != null)
        {
            super.resetTask();
            job.setStructure(null);
            job.setWorkOrder(null);
            resetCurrentStructure();
            getOwnBuilding(AbstractBuildingStructureBuilder.class).setProgressPos(null, StructureIterator.Stage.CLEAR);
            return true;
        }
        else return job.getWorkOrder() != null
                      && ( !world.isBlockLoaded(job.getWorkOrder().getBuildingLocation())
                             || (currentStructure != null && !world.isBlockLoaded(incrementBlock(currentStructure.getCurrentBlockPosition(), new BlockPos(currentStructure.getWidth(), currentStructure.getLength(), currentStructure.getHeight())))));
    }

    /**
     * Increment the block position from an existing position and the size.
     * @param pos the initital position.
     * @param size the max size.
     * @return the next position.
     */
    private static BlockPos incrementBlock(final BlockPos pos, final BlockPos size)
    {
        final BlockPos.MutableBlockPos progressPos = new BlockPos.MutableBlockPos(pos);
        progressPos.setPos(progressPos.getX() + 1, progressPos.getY(), progressPos.getZ());
        if (progressPos.getX() == size.getX())
        {
            progressPos.setPos(0, progressPos.getY(), progressPos.getZ() + 1);
            if (progressPos.getZ() == size.getZ())
            {
                progressPos.setPos(progressPos.getX(), progressPos.getY() + 1, 0);
                if (progressPos.getY() == size.getY())
                {
                    return pos;
                }
            }
        }

        return progressPos;
    }

    @Override
    protected boolean isAlreadyCleared()
    {
        return job.getWorkOrder() != null && job.getWorkOrder().isCleared();
    }

    @Override
    protected void onStartWithoutStructure()
    {
        if (job.getWorkOrder() != null)
        {
            loadStructure();
        }
    }

    /**
     * Check how much of a certain stuck is actually required.
     *
     * @param stack the stack to check.
     * @return the new stack with the correct amount.
     */
    @Override
    @Nullable
    public ItemStack getTotalAmount(@Nullable final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return null;
        }
        final int hashCode = stack.hasTag() ? stack.getTag().hashCode() : 0;
        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding(AbstractBuildingStructureBuilder.class);
        BuildingBuilderResource resource = buildingWorker.getNeededResources().get(stack.getTranslationKey() + "-" + hashCode);

        if(resource == null)
        {
            requestMaterials();
            resource = buildingWorker.getNeededResources().get(stack.getTranslationKey() + "-" + hashCode);
        }

        if(resource == null)
        {
            return stack;
        }

        final ItemStack resStack = new ItemStack(resource.getItem(), Math.min(STACKSIZE, resource.getAmount()));
        resStack.setTag(resource.getItemStack().getTag());
        return resStack;
    }

    @Override
    public void handleSpecificCancelActions()
    {
        getOwnBuilding().getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
        job.setWorkOrder(null);
    }
}
