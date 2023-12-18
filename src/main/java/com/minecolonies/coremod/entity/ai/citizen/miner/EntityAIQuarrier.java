package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.modules.QuarryModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobQuarrier;
import com.minecolonies.coremod.colony.workorders.WorkOrderMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import com.minecolonies.coremod.entity.ai.util.WorkerLoadOnlyStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.BLOCK_PLACE_SPEED;
import static com.minecolonies.api.util.constant.CitizenConstants.PROGRESS_MULTIPLIER;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.BLOCKS_MINED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ORES_MINED;
import static com.minecolonies.api.util.constant.TranslationConstants.QUARRY_MINER_FINISHED_QUARRY;
import static com.minecolonies.api.util.constant.TranslationConstants.QUARRY_MINER_NO_QUARRY;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner.FILL_BLOCK;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure.ItemCheckResult.RECALC;
import static com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner.RENDER_META_PICKAXE;
import static com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner.RENDER_META_SHOVEL;
import static com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler.Stage.*;

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
            final WorkOrderMiner wo = new WorkOrderMiner(quarry.getStructurePack(), shaft.getA(), shaft.getB(), quarry.getRotation(), quarry.getPosition().below(2), false, building.getPosition());
            building.getColony().getWorkManager().addWorkOrder(wo, false);
            job.setWorkOrder(wo);
        }

        return super.loadRequirements();
    }

    /**
     * Get the path of the quarry shaft blueprint.
     *
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
                Log.getLogger().warn("Couldn't find structure with name: " + workOrder.getStructurePath() + " in: " + workOrder.getStructurePack() + ". Aborting loading procedure");
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
        if (structurePlacer.getB().getStage() == null)
        {
            return PICK_UP_RESIDUALS;
        }

        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        checkForExtraBuildingActions();

        // some things to do first! then we go to the actual phase!

        //Fill workFrom with the position from where the builder should build.
        //also ensure we are at that position.
        final BlockPos progress = getProgressPos() == null ? NULL_POS : getProgressPos().getA();
        final BlockPos worldPos = structurePlacer.getB().getProgressPosInWorld(progress);
        if (getProgressPos() != null)
        {
            structurePlacer.getB().setStage(getProgressPos().getB());
        }

        if (!progress.equals(NULL_POS) && !limitReached && (blockToMine == null ? !walkToConstructionSite(worldPos) : !walkToConstructionSite(blockToMine)))
        {
            return getState();
        }

        limitReached = false;

        final StructurePhasePlacementResult result;
        final StructurePlacer placer = structurePlacer.getA();
        switch (structurePlacer.getB().getStage())
        {
            case BUILD_SOLID:
                result = placer.executeStructureStep(world,
                  null,
                  progress,
                  StructurePlacer.Operation.BLOCK_PLACEMENT,
                  () -> placer.getIterator()
                    .decrement(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !BlockUtils.isAnySolid(info.getBlockInfo().getState())
                                                                                 || isDecoItem(info.getBlockInfo().getState().getBlock())
                                                                                 || pos.getY()  < worldPos.getY())),
                  false);

                if (progress.getY() != -1 && result.getIteratorPos().getY() < progress.getY())
                {
                    structurePlacer.getB().nextStage();
                    this.storeProgressPos(new BlockPos(0, progress.getY() + 1, 0), structurePlacer.getB().getStage());
                }
                else
                {
                    this.storeProgressPos(result.getIteratorPos(), structurePlacer.getB().getStage());
                }

                break;
            case DECORATE:

                if (progress.getY() >= structurePlacer.getB().getBluePrint().getSizeY())
                {
                    structurePlacer.getB().nextStage();
                    this.storeProgressPos(new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(), progress.getY() - 1, structurePlacer.getB().getBluePrint().getSizeZ() - 1), structurePlacer.getB().getStage());
                    return getState();
                }

                // not solid
                result = placer.executeStructureStep(world,
                  null,
                  progress,
                  StructurePlacer.Operation.BLOCK_PLACEMENT,
                  () -> placer.getIterator()
                    .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> (BlockUtils.isAnySolid(info.getBlockInfo().getState())
                                                                                 && !isDecoItem(info.getBlockInfo().getState().getBlock()))
                                                                                 || pos.getY() > worldPos.getY())),
                  false);

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    structurePlacer.getB().nextStage();
                    this.storeProgressPos(new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(), progress.getY() - 1, structurePlacer.getB().getBluePrint().getSizeZ() - 1), structurePlacer.getB().getStage());
                }
                else if (progress.getY() != -1 && result.getIteratorPos().getY() > progress.getY())
                {
                    structurePlacer.getB().nextStage();
                    this.storeProgressPos(new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(), progress.getY() - 1, structurePlacer.getB().getBluePrint().getSizeZ() - 1), structurePlacer.getB().getStage());
                }
                else
                {
                    this.storeProgressPos(result.getIteratorPos(), structurePlacer.getB().getStage());
                }
                break;
            case CLEAR:
            default:
                result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.BLOCK_REMOVAL,
                  () -> placer.getIterator().decrement((info, pos, handler) -> handler.getWorld().getBlockState(pos).getBlock() instanceof IBuilderUndestroyable
                                                                                 || handler.getWorld().getBlockState(pos).getBlock() == Blocks.BEDROCK
                                                                                 || handler.getWorld().getBlockState(pos).getBlock() instanceof AirBlock
                                                                                 || info.getBlockInfo().getState().getBlock()
                                                                                      == com.ldtteam.structurize.blocks.ModBlocks.blockFluidSubstitution.get()
                                                                                 || !handler.getWorld().getBlockState(pos).getFluidState().isEmpty()), false);
                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    building.nextStage();
                    building.setProgressPos(null, null);
                    return COMPLETE_BUILD;
                }
                else if (progress.getY() != -1 && (result.getIteratorPos().getY() < progress.getY() || result.getBlockResult().getWorldPos().getY() < worldPos.getY()))
                {
                    structurePlacer.getB().setStage(BUILD_SOLID);
                    this.storeProgressPos(new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(), progress.getY() - 1, structurePlacer.getB().getBluePrint().getSizeZ() - 1), structurePlacer.getB().getStage());
                }
                else
                {
                    this.storeProgressPos(result.getIteratorPos(), structurePlacer.getB().getStage());
                }
                break;
        }

        if (result.getBlockResult().getResult() == BlockPlacementResult.Result.LIMIT_REACHED)
        {
            this.limitReached = true;
        }

        if (result.getBlockResult().getResult() == BlockPlacementResult.Result.MISSING_ITEMS)
        {
            if (hasListOfResInInvOrRequest(this, result.getBlockResult().getRequiredItems(), result.getBlockResult().getRequiredItems().size() > 1) == RECALC)
            {
                job.getWorkOrder().setRequested(false);
                return LOAD_STRUCTURE;
            }
            return NEEDS_ITEM;
        }

        if (result.getBlockResult().getResult() == BlockPlacementResult.Result.BREAK_BLOCK)
        {
            final BlockPos currentWorldPos = result.getBlockResult().getWorldPos();
            if (currentWorldPos.getY() < worker.level.getMinBuildHeight() + 5)
            {
                building.setProgressPos(null, null);
                return COMPLETE_BUILD;
            }

            blockToMine = currentWorldPos;
            return MINE_BLOCK;
        }

        if (MineColonies.getConfig().getServer().builderBuildBlockDelay.get() > 0)
        {
            final double decrease = 1 - worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(BLOCK_PLACE_SPEED);

            setDelay((int) (
              (MineColonies.getConfig().getServer().builderBuildBlockDelay.get() * PROGRESS_MULTIPLIER / (getPlaceSpeedLevel() / 2 + PROGRESS_MULTIPLIER))
                * decrease));
        }
        return getState();
    }

    @Override
    public boolean requestMaterials()
    {
        StructurePhasePlacementResult result;
        final WorkerLoadOnlyStructureHandler structure = new WorkerLoadOnlyStructureHandler(world, structurePlacer.getB().getWorldPos(), structurePlacer.getB().getBluePrint(), new PlacementSettings(), true, this);
        job.getWorkOrder().setIteratorType("default");

        final StructurePlacer placer = new StructurePlacer(structure, job.getWorkOrder().getIteratorType());

        if (requestProgress == null)
        {
            final AbstractBuildingStructureBuilder buildingWorker = building;
            buildingWorker.resetNeededResources();
            requestProgress = new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(),
              structurePlacer.getB().getBluePrint().getSizeY() - 1,
              structurePlacer.getB().getBluePrint().getSizeZ() - 1);
            requestState = RequestStage.SOLID;
        }

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
                                                                                 || isDecoItem(info.getBlockInfo().getState().getBlock())
                                                                                 || pos.getY()  < worldPos.getY())),
                  false);

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }


                if (requestProgress.getY() != -1 && result.getIteratorPos().getY() < requestProgress.getY())
                {
                    requestProgress = new BlockPos(0, requestProgress.getY() + 1, 0);
                    requestState = RequestStage.DECO;
                }
                else if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestProgress = new BlockPos(0, structurePlacer.getB().getBluePrint().getSizeY() - 2, 0);
                    requestState = RequestStage.DECO;
                }
                else
                {
                    requestProgress = result.getIteratorPos();
                }

                return false;
            case DECO:
                if (requestProgress.getY() >= structurePlacer.getB().getBluePrint().getSizeY())
                {
                    requestState = RequestStage.ENTITIES;
                    requestProgress = new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(),
                      requestProgress.getY() - 1,
                      structurePlacer.getB().getBluePrint().getSizeZ() - 1);
                    return false;
                }

                result = placer.executeStructureStep(world,
                  null,
                  requestProgress,
                  StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator()
                    .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> BlockUtils.isAnySolid(info.getBlockInfo().getState()) && !isDecoItem(info.getBlockInfo()
                      .getState()
                      .getBlock())  || pos.getY() > worldPos.getY())),
                  false);

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    building.addNeededResource(stack, stack.getCount());
                }

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.ENTITIES;
                    requestProgress =
                      new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(), requestProgress.getY() - 1, structurePlacer.getB().getBluePrint().getSizeZ() - 1);
                }
                else if (requestProgress.getY() != -1 && result.getIteratorPos().getY() > requestProgress.getY())
                {
                    requestState = RequestStage.ENTITIES;
                    requestProgress =
                      new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(), requestProgress.getY() - 1, structurePlacer.getB().getBluePrint().getSizeZ() - 1);
                }
                else
                {
                    requestProgress = result.getIteratorPos();
                }
                return false;
            case ENTITIES:
                result = placer.executeStructureStep(world, null, requestProgress, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator().decrement(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> info.getEntities().length == 0  || pos.getY()  < worldPos.getY())), true);

                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    requestState = RequestStage.SOLID;
                    requestProgress = null;
                    return true;
                }
                else if (requestProgress.getY() != -1 && (result.getIteratorPos().getY() < requestProgress.getY()))
                {
                    requestState = RequestStage.SOLID;
                    requestProgress = new BlockPos(structurePlacer.getB().getBluePrint().getSizeX(),
                      requestProgress.getY() - 1,
                      structurePlacer.getB().getBluePrint().getSizeZ() - 1);
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
        if (job.findQuarry() == null)
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(QUARRY_MINER_NO_QUARRY), ChatPriority.BLOCKING));
            return true;
        }
        else if (job.findQuarry().getFirstModuleOccurance(QuarryModule.class).isFinished())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(QUARRY_MINER_FINISHED_QUARRY), ChatPriority.BLOCKING));
            return true;
        }
        else if(job.getWorkOrder() != null && !job.getWorkOrder().getLocation().equals(job.findQuarry().getPosition().below(2)))
        {
            blockToMine = null;
            job.complete();
            building.setProgressPos(null, null);
            return true;
        }
        return super.checkIfCanceled();
    }

    @Override
    public void setStructurePlacer(final BuildingStructureHandler<JobQuarrier, BuildingMiner> structure)
    {
        structurePlacer = new Tuple<>(new StructurePlacer(structure, "default"), structure);
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
            final BlockState surroundingState = world.getBlockState(pos);

            final FluidState fluid = world.getFluidState(pos);
            if (surroundingState.getBlock() == Blocks.LAVA || (fluid != null && !fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA)) || SurfaceType.isWater(world, pos, surroundingState, fluid))
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
        blockToMine  = null;
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
     * @param location the place to place the block at.
     * @param block the block.
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
    protected void triggerMinedBlock(@NotNull final BlockState blockToMine)
    {
        super.triggerMinedBlock(blockToMine);
        if (IColonyManager.getInstance().getCompatibilityManager().isOre(blockToMine))
        {
            building.getColony().getStatisticsManager().increment(ORES_MINED);
        }
        building.getColony().getStatisticsManager().increment(BLOCKS_MINED);
    }
}
