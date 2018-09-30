package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.compatibility.candb.ChiselAndBitsCheck;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.schematic.BlockSolidSubstitution;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildRemoval;
import com.minecolonies.coremod.entity.ai.util.Structure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.Suppression.LOOPS_SHOULD_NOT_CONTAIN_MORE_THAN_A_SINGLE_BREAK_OR_CONTINUE_STATEMENT;
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
    public void storeProgressPos(final BlockPos blockPos, final Structure.Stage stage)
    {
        getOwnBuilding(AbstractBuildingStructureBuilder.class).setProgressPos(blockPos, stage);
    }

    @Override
    public Tuple<BlockPos, Structure.Stage> getProgressPos()
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
                final AbstractBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Worker (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getCitizenColonyHandler().getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return;
                }

                worker.getCitizenChatHandler().sendLocalizedChat(COM_MINECOLONIES_COREMOD_ENTITY_BUILDER_BUILDSTART, job.getStructure().getName());

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
        requestMaterials();
        if (getProgressPos() != null)
        {
            job.getStructure().setLocalPosition(getProgressPos().getFirst());
        }
    }

    /**
     * Iterates through all the required resources and stores them in the building.
     * Suppressing Sonar Rule Squid:S135
     * The rule thinks we should have less continue and breaks.
     * But in this case the rule does not apply because code would become unreadable and uneffective without.
     */
    @SuppressWarnings(LOOPS_SHOULD_NOT_CONTAIN_MORE_THAN_A_SINGLE_BREAK_OR_CONTINUE_STATEMENT)
    private void requestMaterials()
    {
        if (Configurations.gameplay.builderInfiniteResources || job.getWorkOrder().isRequested() || job.getWorkOrder() instanceof WorkOrderBuildRemoval)
        {
            return;
        }

        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding(AbstractBuildingStructureBuilder.class);
        buildingWorker.resetNeededResources();

        while (job.getStructure().findNextBlock())
        {
            @Nullable final Template.BlockInfo blockInfo = job.getStructure().getBlockInfo();
            @Nullable final Template.EntityInfo entityInfo = job.getStructure().getEntityinfo();

            if (entityInfo != null)
            {
                for (final ItemStack stack : ItemStackUtils.getListOfStackForEntityInfo(entityInfo, world, worker))
                {
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        buildingWorker.addNeededResource(stack, 1);
                    }
                }
            }

            if (blockInfo == null)
            {
                continue;
            }

            @Nullable IBlockState blockState = blockInfo.blockState;
            @Nullable Block block = blockState.getBlock();

            if (job.getStructure().isStructureBlockEqualWorldBlock()
                  || (blockState.getBlock() instanceof BlockBed && blockState.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.FOOT))
                  || (blockState.getBlock() instanceof BlockDoor && blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.UPPER)))
            {
                continue;
            }

            if (block instanceof BlockSolidSubstitution)
            {
                blockState = getSolidSubstitution(job.getStructure().getBlockPosition());
                block = blockState.getBlock();
            }

            final Block worldBlock = BlockPosUtil.getBlock(world, job.getStructure().getBlockPosition());
            if (block instanceof BlockFalling )
            {
                final IBlockState downState = BlockPosUtil.getBlockState(world, job.getStructure().getBlockPosition().down());
                if (!downState.getMaterial().isSolid())
                {
                    requestBlockToBuildingIfRequired(buildingWorker, getSolidSubstitution(job.getStructure().getBlockPosition()));
                }
            }

            if (block != null
                  && block != Blocks.AIR
                  && worldBlock != Blocks.BEDROCK
                  && !(worldBlock instanceof AbstractBlockHut)
                  && !isBlockFree(block, 0))
            {
                requestBlockToBuildingIfRequired(buildingWorker, blockState);
            }
        }
        job.getWorkOrder().setRequested(true);
    }

    /**
     * Add blocks to the builder building if he needs it.
     *
     * @param building   the building.
     * @param blockState the block to add.
     */
    private void requestBlockToBuildingIfRequired(final AbstractBuildingStructureBuilder building, final IBlockState blockState)
    {
        if (job.getStructure().getBlockInfo().tileentityData != null)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.addAll(getItemsFromTileEntity());

            for (final ItemStack stack : itemList)
            {
                building.addNeededResource(stack, 1);
            }
        }

        if (!ChiselAndBitsCheck.isChiselAndBitsBlock(blockState)
              && blockState.getBlock() != Blocks.BED
              && blockState.getBlock() != Blocks.STANDING_BANNER
              && blockState.getBlock() != Blocks.WALL_BANNER)
        {
            building.addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
        }
    }

    @Override
    public void registerBlockAsNeeded(final ItemStack stack)
    {
        final int hashCode = stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0;
        if (getOwnBuilding(AbstractBuildingStructureBuilder.class)
              .getNeededResources()
              .get(stack.getTranslationKey()
                     + ":" + stack.getItemDamage()
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
        final int hashCode = deliveredItemStack.hasTagCompound() ? deliveredItemStack.getTagCompound().hashCode() : 0;
        final BuildingBuilderResource resource
                = getOwnBuilding(AbstractBuildingStructureBuilder.class)
                .getNeededResources()
                .get(deliveredItemStack.getTranslationKey()
                        + ":" + deliveredItemStack.getItemDamage()
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

        final String structureName = job.getStructure().getName();
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
            final WorkOrderBuildBuilding woh = (wo instanceof WorkOrderBuildBuilding) ? (WorkOrderBuildBuilding) wo : null;
            if (woh != null)
            {
                final AbstractBuilding building = job.getColony().getBuildingManager().getBuilding(wo.getBuildingLocation());
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
            job.complete();
        }
        getOwnBuilding(AbstractBuildingStructureBuilder.class).resetNeededResources();
        resetTask();
    }

    @Override
    public List<ItemStack> getItemsFromTileEntity()
    {
        if (job.getStructure() != null && job.getStructure().getBlockInfo() != null && job.getStructure().getBlockInfo().tileentityData != null)
        {
            return ItemStackUtils.getItemStacksOfTileEntity(job.getStructure().getBlockInfo().tileentityData, world);
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
            getOwnBuilding(AbstractBuildingStructureBuilder.class).setProgressPos(null, Structure.Stage.CLEAR);
            return true;
        }
        return false;
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

    @Override
    public Template.EntityInfo getEntityInfo()
    {
        if (job.getStructure() != null && job.getStructure().getEntityinfo() != null)
        {
            return job.getStructure().getEntityinfo();
        }
        return null;
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
        final int hashCode = stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0;
        final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding(AbstractBuildingStructureBuilder.class);
        final BuildingBuilderResource resource = buildingWorker.getNeededResources().get(stack.getTranslationKey() + ":" + stack.getItemDamage() + "-" + hashCode);

        if(resource == null)
        {
            return stack;
        }
        final ItemStack resStack = new ItemStack(resource.getItem(), Math.min(STACKSIZE, resource.getAmount()), resource.getDamageValue());
        resStack.setTagCompound(resource.getItemStack().getTagCompound());
        return resStack;
    }

    @Override
    public void handleSpecificCancelActions()
    {
        getOwnBuilding().getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
        job.setWorkOrder(null);
    }
}
