package com.minecolonies.coremod.entity.ai.citizen.builder;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.BlockSolidSubstitution;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.jobs.JobBuilder;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuild;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.BlockUtils;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructure<JobBuilder>
{
    /**
     * How often should intelligence factor into the builders skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should strength factor into the builders skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 1;

    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int ACTIONS_UNTIL_DUMP = 1024;

    /**
     * Position where the Builders constructs from.
     */
    @Nullable
    private BlockPos workFrom = null;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, START_WORKING),
                new AITarget(this::checkIfExecute, this::getState),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding)
        );
        worker.setSkillModifier(INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final IBlockState worldMetadata)
    {
        return false;
    }

    /**
     * Load the struction into the AI.
     */
    private void loadStructure()
    {
        WorkOrderBuild workOrder = null;
        workOrder = job.getWorkOrder();

        if (workOrder == null)
        {
            return;
        }

        final BlockPos pos = workOrder.getBuildingLocation();
        if (!(workOrder instanceof WorkOrderBuildDecoration) && worker.getColony().getBuilding(pos) == null)
        {
            Log.getLogger().warn("AbstractBuilding does not exist - removing build request");
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        int tempRotation = 0;
        if (workOrder.getRotation() == 0 && !(workOrder instanceof WorkOrderBuildDecoration))
        {
            final IBlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof AbstractBlockHut)
            {
                tempRotation = BlockUtils.getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING));
            }
        }
        else
        {
            tempRotation = workOrder.getRotation();
        }

        loadStructure(workOrder.getStructureName(), tempRotation, pos, workOrder.isMirrored());

        workOrder.setCleared(false);
        workOrder.setRequested(false);

        //We need to deal with materials
        requestMaterialsIfRequired();
    }

    @Override
    public IBlockState getSolidSubstitution(@NotNull final BlockPos location)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(world, location);
    }

    private boolean checkIfExecute()
    {
        setDelay(1);

        if (!job.hasWorkOrder())
        {
            return true;
        }

        final WorkOrderBuild wo = job.getWorkOrder();

        if (job.getColony().getBuilding(wo.getBuildingLocation()) == null && !(wo instanceof WorkOrderBuildDecoration))
        {
            job.complete();
            return true;
        }

        if (!job.hasStructure())
        {
            initiate();
        }

        return false;
    }

    @Override
    public void requestMaterialsIfRequired()
    {
        if (!Configurations.builderInfiniteResources)
        {
            requestMaterials();
        }
    }

    /**
     * Iterates through all the required resources and stores them in the building.
     * Suppressing Sonar Rule Squid:S135
     * The rule thinks we should have less continue and breaks.
     * But in this case the rule does not apply because code would become unreadable and uneffective without.
     */
    @SuppressWarnings("squid:S135")
    private void requestMaterials()
    {
        if (job.getWorkOrder().isRequested())
        {
            return;
        }
        
        while (job.getStructure().findNextBlock())
        {
            @Nullable final Template.BlockInfo blockInfo = job.getStructure().getBlockInfo();
            @Nullable final Template.EntityInfo entityInfo = job.getStructure().getEntityinfo();

            if (entityInfo != null)
            {
                requestEntityToBuildingIfRequired(entityInfo);
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

            if(block instanceof BlockSolidSubstitution)
            {
                blockState = getSolidSubstitution(job.getStructure().getBlockPosition());
                block = blockState.getBlock();
            }

            final Block worldBlock = BlockPosUtil.getBlock(world, job.getStructure().getBlockPosition());

            if (block != null
                    && block != Blocks.AIR
                    && worldBlock != Blocks.BEDROCK
                    && !(worldBlock instanceof AbstractBlockHut)
                    && !isBlockFree(block, 0))
            {
                requestBlockToBuildingIfRequired((BuildingBuilder) getOwnBuilding(), blockState);
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
    private void requestBlockToBuildingIfRequired(BuildingBuilder building, IBlockState blockState)
    {
        if (((JobBuilder) job).getStructure().getBlockInfo().tileentityData != null)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.addAll(getItemsFromTileEntity());

            for (final ItemStack stack : itemList)
            {
                building.addNeededResource(stack, 1);
            }
        }

        building.addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
    }

    /**
     * Adds entities to the builder building if he needs it.
     */
    private void requestEntityToBuildingIfRequired(Template.EntityInfo entityInfo)
    {
        if (entityInfo != null)
        {
            final Entity entity = getEntityFromEntityInfoOrNull(entityInfo);

            if (entity != null)
            {
                final List<ItemStack> request = new ArrayList<>();
                if (entity instanceof EntityItemFrame)
                {
                    final ItemStack stack = ((EntityItemFrame) entity).getDisplayedItem();
                    if (stack != null)
                    {
                        stack.stackSize = 1;
                        request.add(stack);
                        request.add(new ItemStack(Items.ITEM_FRAME, 1, stack.getItemDamage()));
                    }
                }
                else if (entity instanceof EntityArmorStand)
                {
                    request.add(entity.getPickedResult(new RayTraceResult(worker)));
                    entity.getArmorInventoryList().forEach(request::add);
                }
                else
                {
                    request.add(entity.getPickedResult(new RayTraceResult(worker)));
                }

                for (final ItemStack stack : request)
                {
                    final BuildingBuilder building = (BuildingBuilder) getOwnBuilding();
                    if (stack != null && stack.getItem() != null)
                    {
                        building.addNeededResource(stack, 1);
                    }
                }
            }
        }
    }

    @Override
    public void reduceNeededResources(final ItemStack stack)
    {
        ((BuildingBuilder) getOwnBuilding()).reduceNeededResource(stack, 1);
    }

    private void initiate()
    {
        if (!job.hasStructure())
        {
            workFrom = null;
            loadStructure();
            final WorkOrderBuild wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                        String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)",
                                worker.getColony().getID(),
                                worker.getCitizenData().getId(), job.getWorkOrderId()));
                return;
            }

            if (wo instanceof WorkOrderBuildDecoration)
            {
                LanguageHandler.sendPlayersMessage(worker.getColony().getMessageEntityPlayers(),
                        "entity.builder.messageBuildStart",
                        wo.getName());
            }
            else
            {
                final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                            String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                                    worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return;
                }

                LanguageHandler.sendPlayersMessage(worker.getColony().getMessageEntityPlayers(),
                        "entity.builder.messageBuildStart",
                        job.getStructure().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                }
            }
        }
    }

    @Override
    protected boolean checkIfCanceled()
    {
        if (job.getWorkOrder() == null)
        {
            super.resetTask();
            workFrom = null;
            job.setStructure(null);
            job.setWorkOrder(null);
            resetCurrentStructure();
            return true;
        }
        return false;
    }

    @Override
    protected void onStartWithoutStructure()
    {
        if (job.getWorkOrder() != null)
        {
            loadStructure();
        }
    }

    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return START_BUILDING;
    }

    @Override
    public List<ItemStack> getItemsFromTileEntity()
    {
        if (job.getStructure() != null && job.getStructure().getBlockInfo() != null && job.getStructure().getBlockInfo().tileentityData != null)
        {
            return getItemStacksOfTileEntity(job.getStructure().getBlockInfo().tileentityData);
        }
        return Collections.emptyList();
    }

    /**
     * Get itemStack of tileEntityData. Retrieve the data from the tileEntity.
     *
     * @param compound the tileEntity stored in a compound.
     * @return the list of itemstacks.
     */
    private List<ItemStack> getItemStacksOfTileEntity(NBTTagCompound compound)
    {
        final List<ItemStack> items = new ArrayList<>();
        final TileEntity tileEntity = TileEntity.create(world, compound);
        if (tileEntity instanceof TileEntityFlowerPot)
        {
            items.add(((TileEntityFlowerPot) tileEntity).getFlowerItemStack());
        }
        else if (tileEntity instanceof TileEntityLockable)
        {
            for (int i = 0; i < ((TileEntityLockable) tileEntity).getSizeInventory(); i++)
            {
                final ItemStack stack = ((TileEntityLockable) tileEntity).getStackInSlot(i);
                if (stack != null)
                {
                    items.add(stack);
                }
            }
        }
        return items;
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
        final AbstractBuildingWorker buildingWorker = getOwnBuilding();

        if(stack == null || stack.getItem() == null)
        {
            return null;
        }
        final BuildingBuilderResource resource = ((BuildingBuilder) buildingWorker).getNeededResources().get(stack.getUnlocalizedName());
        return resource == null ? stack : new ItemStack(resource.getItem(), resource.getAmount(), resource.getDamageValue());
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
        LanguageHandler.sendPlayersMessage(worker.getColony().getMessageEntityPlayers(),
                "entity.builder.messageBuildComplete",
                structureName);


        final WorkOrderBuild wo = job.getWorkOrder();
        if (wo == null)
        {
            Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing work order(%d)",
                    worker.getColony().getID(),
                    worker.getCitizenData().getId(),
                    ((JobBuilder) job).getWorkOrderId()));
        }
        else
        {
            if (wo instanceof WorkOrderBuildDecoration && structureName.contains(WAYPOINT_STRING))
            {
                worker.getColony().addWayPoint(wo.getBuildingLocation(), world.getBlockState(wo.getBuildingLocation()));
            }
            else
            {
                final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                            worker.getColony().getID(),
                            worker.getCitizenData().getId(),
                            wo.getBuildingLocation()));
                }
                else
                {
                    building.setBuildingLevel(wo.getUpgradeLevel());
                }
            }
            ((JobBuilder) job).complete();
        }

        final BuildingBuilder workerBuilding = (BuildingBuilder) getOwnBuilding();
        workerBuilding.resetNeededResources();

        resetTask();
    }

    @Override
    public void handleFlowerPots(@NotNull final BlockPos pos)
    {
        if (job.getStructure().getBlockInfo().tileentityData != null)
        {
            final TileEntityFlowerPot tileentityflowerpot = (TileEntityFlowerPot) world.getTileEntity(pos);
            tileentityflowerpot.readFromNBT(((JobBuilder) job).getStructure().getBlockInfo().tileentityData);
            world.setTileEntity(pos, tileentityflowerpot);
        }
    }

    @Override
    public void connectChestToBuildingIfNecessary(@NotNull final BlockPos pos)
    {
        final BlockPos buildingLocation = ((JobBuilder) job).getWorkOrder().getBuildingLocation();
        final AbstractBuilding building = this.getOwnBuilding().getColony().getBuilding(buildingLocation);

        if (building != null)
        {
            building.addContainerPosition(pos);
        }
    }

    @Override
    protected boolean isAlreadyCleared()
    {
        return job.getWorkOrder() != null && job.getWorkOrder().isCleared();
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }
}
