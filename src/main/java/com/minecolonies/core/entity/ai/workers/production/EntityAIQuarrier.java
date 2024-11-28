package com.minecolonies.core.entity.ai.workers.production;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.core.entity.ai.workers.util.LayerBlueprintIterator;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.QuarryModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobQuarrier;
import com.minecolonies.core.colony.workorders.WorkOrderMiner;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import com.minecolonies.core.entity.ai.workers.util.WorkerLoadOnlyStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.QUARRY_MINER_FINISHED_QUARRY;
import static com.minecolonies.api.util.constant.TranslationConstants.QUARRY_MINER_NO_QUARRY;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner.FILL_BLOCK;
import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.RENDER_META_PICKAXE;
import static com.minecolonies.core.entity.ai.workers.production.EntityAIStructureMiner.RENDER_META_SHOVEL;
import static com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler.Stage.*;

/**
 * Class which handles the quarrier behaviour.
 * The quarrier digs out a large hole and builds infrastructure around it.
 */
public class EntityAIQuarrier extends AbstractEntityAIStructureWithWorkOrder<JobQuarrier, BuildingMiner>
{
    private static final String RENDER_META_TORCH = "torch";
    private static final String RENDER_META_STONE = "stone";

    /**
     * Return to chest after 2x building level stacks.
     */
    private static final int MAX_BLOCKS_MINED = 128;

    /**
     * The current Y-level we're calculating the needed resources of
     */
    private int requestLayer;

    /**
     * Constructor for the Quarrier. Defines the tasks the miner executes.
     *
     * @param job a quarrier job to use.
     */
    public EntityAIQuarrier(@NotNull final JobQuarrier job)
    {
        super(job);
        super.registerTargets(
          /*
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(BUILDING_STEP, this::structureStep, STANDARD_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingMiner> getExpectedBuildingClass()
    {
        return BuildingMiner.class;
    }

    //Miner wants to work but is not at building
    @NotNull
    private IAIState startWorkingAtOwnBuilding()
    {
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        final IBuilding quarry = job.findQuarry();
        if (quarry == null)
        {
            walkToBuilding();
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(QUARRY_MINER_NO_QUARRY), ChatPriority.BLOCKING));
            return IDLE;
        }

        if (quarry.getFirstModuleOccurance(QuarryModule.class).isFinished())
        {
            walkToBuilding();
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(QUARRY_MINER_FINISHED_QUARRY), ChatPriority.BLOCKING));
            return IDLE;
        }

        if (walkToBlock(quarry.getPosition()))
        {
            return getState();
        }

        //Miner is at building
        return LOAD_STRUCTURE;
    }

    @Override
    public IAIState loadRequirements()
    {
        if (job.getWorkOrder() == null)
        {
            final IBuilding quarry = job.findQuarry();
            if (quarry == null || quarry.getFirstModuleOccurance(QuarryModule.class).isFinished())
            {
                return IDLE;
            }

            final Tuple<String, String> shaft = getShaftPath(quarry);
            final WorkOrderMiner wo =
              new WorkOrderMiner(quarry.getStructurePack(), shaft.getA(), shaft.getB(), quarry.getRotation(), quarry.getPosition().below(2), false, building.getPosition());
            wo.setClaimedBy(building.getPosition());
            building.getColony().getWorkManager().addWorkOrder(wo, false);
            job.setWorkOrder(wo);
        }

        return super.loadRequirements();
    }

    /**
     * Get the path of the quarry shaft blueprint.
     * <p>
     * The shaft path is either based on an explicit shaft= tag on the building, or based on the actual schematic used.
     * The directory can be explicitly specified, or it will default to the same as the building.
     *
     * @param quarry the quarry building.
     * @return tuple of (structureName, workOrderName) for work order, aka (path, desc).
     */
    private Tuple<String, String> getShaftPath(@NotNull final IBuilding quarry)
    {
        String path = "infrastructure/mineshafts/" + quarry.getSchematicName() + "shaft1.blueprint";

        final AbstractTileEntityColonyBuilding tileEntity = quarry.getTileEntity();
        if (tileEntity != null)
        {
            path = quarry.getBlueprintPath().replace('\\', '/')
                     .replace("1.blueprint", "shaft1.blueprint");
            if (!path.endsWith("shaft1.blueprint"))
            {
                path = path.replace(".blueprint", "shaft.blueprint");
            }

            for (final String tag : tileEntity.getPositionedTags().getOrDefault(BlockPos.ZERO, Collections.emptyList()))
            {
                if (tag.startsWith("shaft="))
                {
                    if (tag.contains("/"))
                    {
                        path = tag.substring(6);
                    }
                    else
                    {
                        path = path.substring(0, path.lastIndexOf('/') + 1) + tag.substring(6);
                    }
                }
                break;
            }

            if (!path.endsWith(".blueprint"))
            {
                path += ".blueprint";
            }
        }

        final String name = path.substring(path.lastIndexOf('/') + 1).replace(".blueprint", "");
        return new Tuple<>(path, name);
    }

    @Override
    public void onBlockDropReception(final List<ItemStack> blockDrops)
    {
        super.onBlockDropReception(blockDrops);
        for (final ItemStack stack : blockDrops)
        {
            building.getModule(STATS_MODULE).incrementBy(ITEM_OBTAINED + ";" + stack.getItem().getDescriptionId(), stack.getCount());
        }
    }

    @Override
    public void loadStructure(
      @NotNull final IWorkOrder workOrder,
      final int rotateTimes,
      final BlockPos position,
      final boolean isMirrored,
      final boolean removal)
    {
        final Future<Blueprint> blueprintFuture = workOrder.getBlueprintFuture();
        this.loadingBlueprint = true;

        ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(blueprintFuture, world, (blueprint -> {
            if (blueprint == null)
            {
                handleSpecificCancelActions();
                Log.getLogger()
                  .warn("Couldn't find structure with name: " + workOrder.getStructurePath() + " in: " + workOrder.getStructurePack() + ". Aborting loading procedure");
                this.loadingBlueprint = false;
                return;
            }

            final BuildingStructureHandler<JobQuarrier, BuildingMiner> structure;
            structure = new BuildingStructureHandler<>(world,
              position,
              blueprint,
              new PlacementSettings(isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotateTimes)),
              this, new BuildingStructureHandler.Stage[] {BUILD_SOLID, DECORATE, CLEAR});
            building.setTotalStages(3);

            if (!structure.hasBluePrint())
            {
                handleSpecificCancelActions();
                Log.getLogger().warn("Couldn't find structure with name: " + workOrder.getStructurePath() + " aborting loading procedure");
                this.loadingBlueprint = false;
                return;
            }

            job.setBlueprint(structure.getBluePrint());
            job.getBlueprint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotateTimes), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, world);
            setStructurePlacer(structure);

            if (getProgressPos() != null)
            {
                structure.setStage(getProgressPos().getB());
            }
            this.loadingBlueprint = false;
        })));
    }

    @Override
    protected IBuilding getBuildingToDump()
    {
        final IBuilding quarry = job.findQuarry();
        return quarry == null ? super.getBuildingToDump() : quarry;
    }

    @Override
    protected IAIState structureStep()
    {
        if (structurePlacer != null && structurePlacer.getA() != null)
        {
            // Make sure the iterator is at the right Y-level
            final LayerBlueprintIterator layerBlueprintIterator = (LayerBlueprintIterator) structurePlacer.getA().getIterator();
            final BlockPos progressPos = getProgressPos() == null ? null : getProgressPos().getA();
            if (progressPos == null)
            {
                // The quarrier starts building at the top
                layerBlueprintIterator.setLayer(layerBlueprintIterator.getSize().getY() - 1);
            }
            else if (!progressPos.equals(NULL_POS))
            {
                layerBlueprintIterator.setLayer(progressPos.getY());
            }
        }

        return super.structureStep();
    }

    @Override
    protected BlockPos getPosToWorkAt()
    {
        final BlockPos progressPos = getProgressPos() == null ? NULL_POS : getProgressPos().getA();
        if (progressPos == NULL_POS)
        {
            return null;
        }
        return structurePlacer.getB().getProgressPosInWorld(progressPos);
    }

    @Override
    protected boolean goToNextStage(StructurePhasePlacementResult result)
    {
        final LayerBlueprintIterator iterator = (LayerBlueprintIterator) structurePlacer.getA().getIterator();
        final int currentLayer = iterator.getLayer();

        if (!super.goToNextStage(result))
        {
            if (currentLayer == 0)
            {
                // Done
                return false;
            }
            // Done with the last stage, going to the next layer
            iterator.setLayer(currentLayer - 1);
            building.setTotalStages(3);
            structurePlacer.getB().setStage(BUILD_SOLID);
        }
        else if (structurePlacer.getB().getStage() == CLEAR && result.getBlockResult().getWorldPos().getY() <= worker.level.getMinBuildHeight())
        {
            // At bedrock level, so we're done
            return false;
        }
        else
        {
            final int newLayer = switch (structurePlacer.getB().getStage())
                                   {
                                       // The quarrier decorates the level above the one they just placed solid blocks at (to support rails and torches standing on those blocks)
                                       case DECORATE -> currentLayer + 1;
                                       // After decorating, we need to go a layer lower again
                                       case CLEAR -> currentLayer - 1;
                                       default -> currentLayer;
                                   };
            if (newLayer >= iterator.getSize().getY())
            {
                // This can happen at the first level when getting to the decoration stage. In that case, skip the decoration step and go to the CLEAR step immediately
                super.goToNextStage(result);
                building.nextStage();
                iterator.setLayer(currentLayer);
            }
            else
            {
                iterator.setLayer(newLayer);
            }
        }
        return true;
    }

    @Override
    protected boolean skipBuilding(final BlueprintPositionInfo info, final BlockPos pos, final IStructureHandler handler)
    {
        final BlockState blockInfoState = info.getBlockInfo().getState();
        return !BlockUtils.isAnySolid(blockInfoState)
                 || isDecoItem(blockInfoState.getBlock())
                 || DONT_TOUCH_PREDICATE.test(info, pos, handler);
    }

    @Override
    protected boolean skipClearing(final BlueprintPositionInfo info, final BlockPos pos, final IStructureHandler handler)
    {
        if (super.skipClearing(info, pos, handler))
        {
            return true;
        }
        final BlockState state = handler.getWorld().getBlockState(pos);
        // The blocks have been placed earlier by the quarrier. E.g. fences and walls can have the wrong blockstate
        // (due to connecting to yet to be mined blocks), which means the block state is incorrect, and they would be mined by the quarrier,
        // only to be replaced after the next restart. Instead, skip them now if the block itself is correct
        return state.getBlock() == info.getBlockInfo().getState().getBlock();
    }

    @Override
    public boolean requestMaterials()
    {
        StructurePhasePlacementResult result;
        final WorkerLoadOnlyStructureHandler structure =
          new WorkerLoadOnlyStructureHandler(world, structurePlacer.getB().getWorldPos(), structurePlacer.getB().getBluePrint(), new PlacementSettings(), true, this);
        job.getWorkOrder().setIteratorType("default");

        final LayerBlueprintIterator iterator = new LayerBlueprintIterator(job.getWorkOrder().getIteratorType(), structure);
        final StructurePlacer placer = new StructurePlacer(structure, iterator);

        if (requestProgress == null)
        {
            final AbstractBuildingStructureBuilder buildingWorker = building;
            buildingWorker.resetNeededResources();
            requestProgress = NULL_POS;
            requestLayer = structurePlacer.getB().getBluePrint().getSizeY() - 1;
            requestState = RequestStage.SOLID;
        }
        else if (requestLayer < 0)
        {
            // Done
            requestState = RequestStage.SOLID;
            requestProgress = null;
            return true;
        }
        iterator.setLayer(requestLayer);

        final BlockPos worldPos = structure.getProgressPosInWorld(requestProgress);
        final RequestStage currState = requestState;
        switch (currState)
        {
            case SOLID:
                result = placer.executeStructureStep(world,
                  null,
                  requestProgress,
                  StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator()
                          .decrement(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !BlockUtils.isAnySolid(info.getBlockInfo().getState())
                                                                                       || isDecoItem(info.getBlockInfo().getState().getBlock()))),
                  false);

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }


                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    if (requestLayer + 1 >= structurePlacer.getB().getBluePrint().getSizeY())
                    {
                        // Skip decoration for the first layer, as we request the materials of the layer above
                        // Continue with the next layer
                        requestLayer = requestLayer - 1;
                        requestProgress = NULL_POS;
                    }
                    else
                    {
                        requestLayer = requestLayer + 1;
                        requestProgress = NULL_POS;
                        requestState = RequestStage.DECO;
                    }
                }
                else
                {
                    requestProgress = result.getIteratorPos();
                }

                return false;
            case DECO:
                result = placer.executeStructureStep(world,
                  null,
                  requestProgress,
                  StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator()
                          .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> BlockUtils.isAnySolid(info.getBlockInfo().getState()) && !isDecoItem(info.getBlockInfo()
                                                                                                                                                            .getState()
                                                                                                                                                            .getBlock()))),
                  false);

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.SOLID;
                    requestLayer = requestLayer - 2;
                    requestProgress = NULL_POS;
                }
                else
                {
                    requestProgress = result.getIteratorPos();
                }
                return false;
        }

        return true;
    }

    @Override
    protected boolean checkIfCanceled()
    {
        boolean isCanceled = false;
        if (job.findQuarry() == null)
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(QUARRY_MINER_NO_QUARRY), ChatPriority.BLOCKING));
            isCanceled = true;
        }
        else if (job.findQuarry().getFirstModuleOccurance(QuarryModule.class).isFinished())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(QUARRY_MINER_FINISHED_QUARRY), ChatPriority.BLOCKING));
            isCanceled = true;
        }
        else if (job.getWorkOrder() != null && !job.getWorkOrder().getLocation().equals(job.findQuarry().getPosition().below(2)))
        {
            isCanceled = true;
        }

        if (isCanceled)
        {
            if (job.hasWorkOrder())
            {
                job.getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
                job.setWorkOrder(null);
            }
            blockToMine = null;
            building.setProgressPos(null, null);
            worker.getCitizenData().setStatusPosition(null);
            return true;
        }
        return super.checkIfCanceled();
    }

    @Override
    public void setStructurePlacer(final BuildingStructureHandler<JobQuarrier, BuildingMiner> structure)
    {
        final LayerBlueprintIterator iterator = new LayerBlueprintIterator("default", structure);
        structurePlacer = new Tuple<>(new StructurePlacer(structure, iterator), structure);
    }

    @Override
    public int getBreakSpeedLevel()
    {
        return getPrimarySkillLevel();
    }

    @Override
    public int getPlaceSpeedLevel()
    {
        return getSecondarySkillLevel();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return building.getBuildingLevel() * MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        StringBuilder renderData = new StringBuilder(getState() == MINER_MINING_SHAFT || getState() == MINE_BLOCK || getState() == BUILDING_STEP ? RENDER_META_WORKING : "");
        final ItemStack block = new ItemStack(getMainFillBlock());

        for (int slot = 0; slot < worker.getInventoryCitizen().getSlots(); slot++)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            if (stack.getItem() == Items.TORCH && renderData.indexOf(RENDER_META_TORCH) == -1)
            {
                renderData.append(RENDER_META_TORCH);
            }
            else if (stack.getItem() == block.getItem() && renderData.indexOf(RENDER_META_STONE) == -1)
            {
                renderData.append(RENDER_META_STONE);
            }
            else if (stack.canPerformAction(ToolActions.PICKAXE_DIG) && renderData.indexOf(RENDER_META_PICKAXE) == -1)
            {
                renderData.append(RENDER_META_PICKAXE);
            }
            else if (stack.canPerformAction(ToolActions.SHOVEL_DIG) && renderData.indexOf(RENDER_META_SHOVEL) == -1)
            {
                renderData.append(RENDER_META_SHOVEL);
            }
        }

        worker.setRenderMetadata(renderData.toString());
    }

    @Override
    public IAIState doMining()
    {
        if (blockToMine == null)
        {
            return BUILDING_STEP;
        }

        for (final Direction direction : Direction.values())
        {
            final BlockPos pos = blockToMine.relative(direction);
            final FluidState fluid = world.getFluidState(pos);
            if (!fluid.isEmpty())
            {
                setBlockFromInventory(pos, getMainFillBlock());
            }
        }

        if (world.getBlockState(blockToMine).getBlock() instanceof AirBlock)
        {
            blockToMine = null;
            return BUILDING_STEP;
        }

        if (!mineBlock(blockToMine, getCurrentWorkingPosition()))
        {
            worker.swing(InteractionHand.MAIN_HAND);
            return getState();
        }

        worker.decreaseSaturationForContinuousAction();
        blockToMine = null;
        return BUILDING_STEP;
    }

    /**
     * Get the main fill block. Based on the settings.
     *
     * @return the main fill block.
     */
    private Block getMainFillBlock()
    {
        return building.getSetting(FILL_BLOCK).getValue().getBlock();
    }

    @Override
    public ItemStack getTotalAmount(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return null;
        }

        final ItemStack copy = stack.copy();
        copy.setCount(Math.max(super.getTotalAmount(stack).getCount(), copy.getMaxStackSize() / 2));
        return copy;
    }

    @Override
    public IAIState afterStructureLoading()
    {
        return BUILDING_STEP;
    }

    /**
     * Handles the placement and reduction of a block from the inventory.
     *
     * @param location the place to place the block at.
     * @param block    the block.
     */
    private void setBlockFromInventory(@NotNull final BlockPos location, final Block block)
    {
        worker.swing(worker.getUsedItemHand());

        final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block);
        if (slot != -1)
        {
            getInventory().extractItem(slot, 1, false);
            //Flag 1+2 is needed for updates
            WorldUtil.setBlockState(world, location, block.defaultBlockState());
        }
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        super.executeSpecificCompleteActions();
        final IBuilding quarry = job.findQuarry();
        if (quarry != null)
        {
            quarry.getFirstModuleOccurance(QuarryModule.class).setFinished();
        }
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return IColonyManager.getInstance().getCompatibilityManager().isOre(worldMetadata);
    }

    @Override
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        if (workFrom == null)
        {
            workFrom = findRandomPositionToWalkTo(5, currentBlock);
            if (workFrom == null && pathBackupFactor > 10)
            {
                workFrom = worker.blockPosition();
            }
            return false;
        }

        if (BlockPosUtil.getDistance(worker.blockPosition(), currentBlock) <= 5 + 5 * pathBackupFactor)
        {
            return true;
        }

        if (walkToBlock(workFrom))
        {
            return false;
        }

        if (BlockPosUtil.getDistance(worker.blockPosition(), currentBlock) > 5 + 5 * pathBackupFactor)
        {
            workFrom = null;
            return false;
        }

        if (pathBackupFactor > 1)
        {
            pathBackupFactor--;
        }

        return true;
    }

    @Override
    public BlockState getSolidSubstitution(final BlockPos ignored)
    {
        return getMainFillBlock().defaultBlockState();
    }

    @Override
    protected void triggerMinedBlock(@NotNull final BlockPos position, @NotNull final BlockState blockToMine)
    {
        super.triggerMinedBlock(position, blockToMine);
        if (IColonyManager.getInstance().getCompatibilityManager().isOre(blockToMine))
        {
            building.getColony().getStatisticsManager().increment(ORES_MINED, building.getColony().getDay());
        }
        building.getColony().getStatisticsManager().increment(BLOCKS_MINED, building.getColony().getDay());
    }
}
