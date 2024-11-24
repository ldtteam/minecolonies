package com.minecolonies.core.entity.ai.workers;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.blocks.schematic.BlockFluidSubstitution;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.workers.util.IBuilderUndestroyable;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.BuildingResourcesModule;
import com.minecolonies.core.colony.buildings.utils.BuilderBucket;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.core.colony.jobs.AbstractJobStructure;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import com.minecolonies.core.tileentities.TileEntityDecorationController;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.BLOCK_PLACE_SPEED;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner.FILL_BLOCK;
import static com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructure.ItemCheckResult.*;
import static com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler.Stage.*;

/**
 * This base ai class is used by ai's who need to build entire structures. These structures have to be supplied as schematics files.
 * <p>
 * Once an ai starts building a structure, control over it is only given back once that is done.
 * <p>
 * If the ai resets, the structure is gone, so just restart building and no progress will be reset.
 *
 * @param <J> the job type this AI has to do.
 */
public abstract class AbstractEntityAIStructure<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> extends AbstractEntityAIInteract<J, B>
{
    /**
     * Building block delay
     */
    protected static final int BUILD_BLOCK_DELAY = 15;

    /**
     * The current structure task to be build.
     */
    protected Tuple<StructurePlacer, BuildingStructureHandler<J, B>> structurePlacer;

    /**
     * If the structure state is currently reached limit rather than block placement.
     */
    protected boolean limitReached = false;

    /**
     * We're waiting for the blueprint to finish loading.
     */
    protected boolean loadingBlueprint = false;

    /**
     * Different item check result possibilities.
     */
    public enum ItemCheckResult
    {
        FAIL,
        SUCCESS,
        RECALC
    }

    /**
     * Predicate defining things we don't want the builders to ever touch.
     */
    protected TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> DONT_TOUCH_PREDICATE = (info, worldPos, handler) ->
    {
        final BlockState worldState = handler.getWorld().getBlockState(worldPos);

        return worldState.getBlock() instanceof IBuilderUndestroyable
                 || worldState.getBlock() == Blocks.BEDROCK
                 || (info.getBlockInfo().getState().getBlock() instanceof AbstractBlockHut && handler.getWorldPos().equals(worldPos)
                       && worldState.getBlock() instanceof AbstractBlockHut);
    };

    /**
     * Position where the Builders constructs from.
     */
    protected BlockPos workFrom;

    /**
     * Block to mine.
     */
    protected BlockPos blockToMine;

    /**
     * Block to go to when building
     */
    protected BlockPos gotoPos = null;

    /**
     * The id in the list of the last picked up item.
     */
    private int pickUpCount = 0;

    /**
     * Creates this ai base class and set's up important things.
     * <p>
     * Always use this constructor!
     *
     * @param job the job class of the ai using this base class.
     */
    protected AbstractEntityAIStructure(@NotNull final J job)
    {
        super(job);
        this.registerTargets(

          /*
           * Pick up stuff which might've been
           */
          new AITarget(PICK_UP_RESIDUALS, this::pickUpResiduals, TICKS_SECOND),
          /*
           * Check if tasks should be executed.
           */
          new AIEventTarget(AIBlockingEventType.STATE_BLOCKING, this::checkIfCanceled, IDLE, 1),
          /*
           * Select the appropriate State to do next.
           */
          new AITarget(LOAD_STRUCTURE, this::loadRequirements, 5),
          /*
           * Select the appropriate State to do next.
           */
          new AITarget(START_BUILDING, this::startBuilding, 1),
          /*
           * Select the appropriate State to do next.
           */
          new AITarget(MINE_BLOCK, this::doMining, 10),
          /*
           * Check if we have to build something.
           */
          new AITarget(IDLE, this::isThereAStructureToBuild, () -> START_BUILDING, 100),
          /*
           * Build the structure and foundation of the building.
           */
          new AITarget(BUILDING_STEP, this::structureStep, STANDARD_DELAY),
          /*
           * Finalize the building and give back control to the ai.
           */
          new AITarget(COMPLETE_BUILD, this::completeBuild, STANDARD_DELAY),
          new AITarget(PICK_UP, this::pickUpMaterial, 5)
        );
    }

    /**
     * State to pick up material before going back to work.
     *
     * @return the next state to go to.
     */
    public IAIState pickUpMaterial()
    {
        if (structurePlacer == null || !structurePlacer.getB().hasBluePrint())
        {
            return IDLE;
        }

        if (structurePlacer.getB().getStage() == null || structurePlacer.getB().getStage() == BuildingStructureHandler.Stage.CLEAR)
        {
            pickUpCount = 0;
            return START_WORKING;
        }

        final List<Tuple<Predicate<ItemStack>, Integer>> neededItemsList = new ArrayList<>();

        final BuilderBucket neededRessourcesMap = building.getRequiredResources();
        final BuildingResourcesModule module = building.getFirstModuleOccurance(BuildingResourcesModule.class);
        if (neededRessourcesMap != null)
        {
            for (final Map.Entry<String, Integer> entry : neededRessourcesMap.getResourceMap().entrySet())
            {
                final BuildingBuilderResource res = module.getResourceFromIdentifier(entry.getKey());
                if (res != null)
                {
                    int amount = entry.getValue();
                    neededItemsList.add(new Tuple<>(itemstack -> ItemStackUtils.compareItemStacksIgnoreStackSize(res.getItemStack(), itemstack, true, true), amount));
                }
            }
        }

        if (neededItemsList.size() <= pickUpCount || InventoryUtils.openSlotCount(worker.getInventoryCitizen()) <= MIN_OPEN_SLOTS)
        {
            building.checkOrRequestBucket(building.getRequiredResources(), worker.getCitizenData());
            building.checkOrRequestBucket(building.getNextBucket(), worker.getCitizenData());
            pickUpCount = 0;
            return START_WORKING;
        }

        needsCurrently = neededItemsList.get(pickUpCount);
        pickUpCount++;

        if (InventoryUtils.hasItemInProvider(building.getTileEntity(), needsCurrently.getA()))
        {
            return GATHERING_REQUIRED_MATERIALS;
        }

        return PICK_UP;
    }

    /**
     * Pick up residuals within the building area.
     *
     * @return the next state to go to.
     */
    protected IAIState pickUpResiduals()
    {
        if (structurePlacer != null && structurePlacer.getB().getStage() != null)
        {
            return IDLE;
        }

        if (getItemsForPickUp() == null)
        {
            fillItemsList();
        }

        if (getItemsForPickUp() != null && !getItemsForPickUp().isEmpty())
        {
            gatherItems();
            return getState();
        }

        resetGatheringItems();
        workFrom = null;
        structurePlacer = null;

        return IDLE;
    }

    /**
     * Completition logic.
     *
     * @return the final state after completition.
     */
    protected IAIState completeBuild()
    {
        incrementActionsDoneAndDecSaturation();
        executeSpecificCompleteActions();
        worker.getCitizenExperienceHandler().addExperience(XP_EACH_BUILDING);

        return PICK_UP_RESIDUALS;
    }

    /**
     * Start building this StructureIterator.
     * <p>
     * Will determine where to start.
     *
     * @return the new State to start in.
     */
    @NotNull
    protected IAIState startBuilding()
    {
        if (structurePlacer == null || !structurePlacer.getB().hasBluePrint())
        {
            return LOAD_STRUCTURE;
        }
        return BUILDING_STEP;
    }

    /**
     * The next state after structure loading.
     *
     * @return the next state.
     */
    public IAIState afterStructureLoading()
    {
        return START_BUILDING;
    }

    /**
     * Walk to the current construction site.
     * <p>
     * Calculates and caches the position where to walk to.
     *
     * @param currentBlock the current block it is working on.
     * @return true while walking to the site.
     */
    public boolean walkToConstructionSite(final BlockPos currentBlock)
    {
        if (workFrom == null)
        {
            workFrom = getWorkingPosition(currentBlock);
        }

        //The miner shouldn't search for a save position. Just let him build from where he currently is.
        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.blockPosition(), workFrom) < MIN_WORKING_RANGE;
    }

    /**
     * Checks for blocks that need to be treated as deco
     */
    protected static boolean isDecoItem(Block block)
    {
        return block.defaultBlockState().is(ModTags.decorationItems) || block instanceof BlockFluidSubstitution;
    }

    /**
     * The Structure step to execute the actual placement actions etc.
     *
     * @return the next step to go to.
     */
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
        final BlockPos worldPos = getPosToWorkAt();

        if (getProgressPos() != null)
        {
            structurePlacer.getB().setStage(getProgressPos().getB());
        }

        if ((worldPos != null || blockToMine != null) && !limitReached && (blockToMine == null
                                                                             ? !walkToConstructionSite(worldPos)
                                                                             : !walkToConstructionSite(blockToMine)))
        {
            return getState();
        }

        limitReached = false;

        final StructurePhasePlacementResult result;
        final StructurePlacer placer = structurePlacer.getA();
        switch (structurePlacer.getB().getStage())
        {
            case BUILD_SOLID:
                //structure
                result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.BLOCK_PLACEMENT,
                  () -> placer.getIterator().increment(this::skipBuilding), false);
                break;
            case WEAK_SOLID:

                // not solid
                result = placer.executeStructureStep(world,
                  null,
                  progress,
                  StructurePlacer.Operation.BLOCK_PLACEMENT,
                  () -> placer.getIterator()
                          .increment(((info, pos, handler) -> !BlockUtils.isWeakSolidBlock(info.getBlockInfo().getState()) || DONT_TOUCH_PREDICATE.test(info, pos, handler))),
                  false);
                break;
            case CLEAR_WATER:
                //water
                result = placer.clearWaterStep(world, progress);
                break;
            case CLEAR_NON_SOLIDS:
                // clear air
                result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.BLOCK_PLACEMENT,
                  () -> placer.getIterator().decrement((info, pos, handler) ->
                                                         !(info.getBlockInfo().getState().getBlock() instanceof AirBlock)
                                                           || (handler.getWorld().isEmptyBlock(pos))
                                                           || DONT_TOUCH_PREDICATE.test(info, pos, handler)), false);
                break;
            case DECORATE:
                // not solid
                result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.BLOCK_PLACEMENT,
                  () -> placer.getIterator().increment(this::skipDecorate), false);
                break;
            case SPAWN:
                if (placer.getHandler().getBluePrint().getEntities().length == 0)
                {
                    result = new StructurePhasePlacementResult(BlockPos.ZERO, new BlockPlacementResult(BlockPos.ZERO, BlockPlacementResult.Result.FINISHED));
                }
                else
                {
                    // entities
                    result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.SPAWN_ENTITY,
                      () -> placer.getIterator().increment((info, pos, handler) -> info.getEntities().length == 0 || DONT_TOUCH_PREDICATE.test(info, pos, handler)), true);
                }
                break;
            case REMOVE_WATER:
                //water
                placer.getIterator().setRemoving();
                result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.WATER_REMOVAL,
                  () -> placer.getIterator().decrement((info, pos, handler) -> info.getBlockInfo().getState().getFluidState().isEmpty()), false);
                break;
            case REMOVE:
                placer.getIterator().setRemoving();
                result = placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.BLOCK_REMOVAL,
                  () -> placer.getIterator().decrement(this::skipRemoval), true);
                break;
            case CLEAR:
            default:
                result =
                  placer.executeStructureStep(world, null, progress, StructurePlacer.Operation.BLOCK_REMOVAL, () -> placer.getIterator().decrement(this::skipClearing), false);
                if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
                {
                    building.checkOrRequestBucket(building.getRequiredResources(), worker.getCitizenData());
                }
                break;
        }

        if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FAIL)
        {
            Log.getLogger().error("Failed placement at: " + result.getBlockResult().getWorldPos().toShortString());
        }

        if (result.getBlockResult().getResult() == BlockPlacementResult.Result.FINISHED)
        {

            building.nextStage();
            if (!goToNextStage(result))
            {
                building.setProgressPos(null, null);
                worker.getCitizenData().setStatusPosition(null);
                return COMPLETE_BUILD;
            }
            this.storeProgressPos(NULL_POS, structurePlacer.getB().getStage());
        }
        else if (result.getBlockResult().getResult() == BlockPlacementResult.Result.LIMIT_REACHED)
        {
            this.limitReached = true;
            this.storeProgressPos(result.getIteratorPos(), structurePlacer.getB().getStage());
        }
        else
        {
            if (structurePlacer.getB().getStage() != CLEAR_WATER && !result.getIteratorPos().equals(NULL_POS))
            {
                gotoPos = result.getIteratorPos();
            }
            else
            {
                gotoPos = null;
            }

            this.storeProgressPos(result.getIteratorPos(), structurePlacer.getB().getStage());
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

        worker.swing(InteractionHand.MAIN_HAND);
        worker.queueSound(SoundEvents.BAMBOO_HIT, worker.blockPosition(), 10, 0, 0.5f, 0.1f);

        if (result.getBlockResult().getResult() == BlockPlacementResult.Result.BREAK_BLOCK)
        {
            blockToMine = result.getBlockResult().getWorldPos();
            worker.getCitizenData().setStatusPosition(blockToMine);
            return MINE_BLOCK;
        }
        else
        {
            blockToMine = null;
        }

        final double decrease = 1 - worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(BLOCK_PLACE_SPEED);
        setDelay((int) ((BUILD_BLOCK_DELAY * PROGRESS_MULTIPLIER / (getPlaceSpeedLevel() / 2 + PROGRESS_MULTIPLIER)) * decrease));

        return getState();
    }

    /**
     * Get the position the citizen needs to walk to in order to build
     *
     * @return the position to go to
     */
    protected BlockPos getPosToWorkAt()
    {
        if (gotoPos != null)
        {
            return structurePlacer.getB().getProgressPosInWorld(gotoPos);
        }
        else
        {
            return job.getWorkOrder().getLocation();
        }
    }

    /**
     * This method is called when a stage is finished, and it is needed to go to the next stage
     *
     * @return whether it succeeded, i.e. there was a next stage to go to.
     */
    protected boolean goToNextStage(StructurePhasePlacementResult result)
    {
        return structurePlacer.getB().nextStage();
    }

    /**
     * Checks which blocks are skipped on decorate
     *
     * @param info
     * @param pos
     * @param handler
     * @return
     */
    private boolean skipDecorate(final BlueprintPositionInfo info, final BlockPos pos, final IStructureHandler handler)
    {
        final BlockState blockInfoState = info.getBlockInfo().getState();
        return (!isDecoItem(blockInfoState.getBlock()) && BlockUtils.isAnySolid(blockInfoState)) || DONT_TOUCH_PREDICATE.test(info, pos, handler);
    }

    /**
     * Checks wich blocks are skipped on building
     *
     * @param info
     * @param pos
     * @param handler
     * @return
     */
    protected boolean skipBuilding(final BlueprintPositionInfo info, final BlockPos pos, final IStructureHandler handler)
    {
        final BlockState blockInfoState = info.getBlockInfo().getState();
        return !BlockUtils.canBlockFloatInAir(blockInfoState)
                 || isDecoItem(blockInfoState.getBlock())
                 || DONT_TOUCH_PREDICATE.test(info, pos, handler);
    }

    /**
     * Checks which blocks are skipped on clearing
     *
     * @param info
     * @param pos
     * @param handler
     * @return
     */
    protected boolean skipClearing(final BlueprintPositionInfo info, final BlockPos pos, final IStructureHandler handler)
    {
        if (info.getBlockInfo().getState().getBlock() == com.ldtteam.structurize.blocks.ModBlocks.blockFluidSubstitution.get())
        {
            return true;
        }

        final BlockState state = handler.getWorld().getBlockState(pos);
        return state.getBlock() instanceof IBuilderUndestroyable
                 || state.getBlock() == Blocks.BEDROCK
                 || state.isAir()
                 || !state.getFluidState().isEmpty();
    }

    /**
     * Checks which blocks are skipped on removal on building upgrade
     *
     * @param info
     * @param pos
     * @param handler
     * @return
     */
    private boolean skipRemoval(final BlueprintPositionInfo info, final BlockPos pos, final IStructureHandler handler)
    {
        final BlockState infoBlockState = info.getBlockInfo().getState();
        final Block infoBlock = infoBlockState.getBlock();
        if (infoBlockState.isAir()
              || infoBlock == com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution.get()
              || infoBlock == com.ldtteam.structurize.blocks.ModBlocks.blockSubstitution.get()
              || infoBlock == com.ldtteam.structurize.blocks.ModBlocks.blockFluidSubstitution.get())
        {
            return true;
        }

        final BlockState state = handler.getWorld().getBlockState(pos);
        return state.isAir() || state.getBlock() instanceof IBuilderUndestroyable || state.getBlock() == Blocks.WATER;
    }

    /**
     * Get the level that affects the place speed.
     *
     * @return the level.
     */
    public abstract int getPlaceSpeedLevel();

    @Override
    public IAIState getStateAfterPickUp()
    {
        return PICK_UP;
    }

    @Override
    public IAIState afterRequestPickUp()
    {
        return INVENTORY_FULL;
    }

    @Override
    public IAIState afterDump()
    {
        return PICK_UP;
    }

    /**
     * Separate step for mining.
     *
     * @return the next state to go to.
     */
    public IAIState doMining()
    {
        if (blockToMine == null)
        {
            return BUILDING_STEP;
        }

        final BlockState worldState = world.getBlockState(blockToMine);
        if (worldState.getBlock() instanceof AirBlock || worldState.getBlock() == Blocks.WATER)
        {
            return BUILDING_STEP;
        }

        if (!walkToConstructionSite(blockToMine))
        {
            return getState();
        }

        if (!mineBlock(blockToMine, getCurrentWorkingPosition()))
        {
            worker.swing(InteractionHand.MAIN_HAND);
            return getState();
        }
        worker.decreaseSaturationForContinuousAction();
        return BUILDING_STEP;
    }

    /**
     * Loads the structure given the name, rotation and position.
     *
     * @param workOrder   the work order.
     * @param rotateTimes number of times to rotateWithMirror it.
     * @param position    the position to set it.
     * @param isMirrored  is the structure mirroed?
     * @param removal     if removal step.
     */
    public void loadStructure(@NotNull final IWorkOrder workOrder, final int rotateTimes, final BlockPos position, final boolean isMirrored, final boolean removal)
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

            final BuildingStructureHandler<J, B> structure;
            IBuilding colonyBuilding = worker.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuilding(position);
            final BlockEntity entity = world.getBlockEntity(position);

            if (removal)
            {
                structure = new BuildingStructureHandler<>(world,
                  position,
                  blueprint,
                  new PlacementSettings(isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotateTimes)),
                  this, new BuildingStructureHandler.Stage[] {REMOVE_WATER, REMOVE});
                building.setTotalStages(2);
            }
            else if ((colonyBuilding != null && (colonyBuilding.getBuildingLevel() > 0 || colonyBuilding.hasParent())) ||
                       (entity instanceof TileEntityDecorationController && Utils.getBlueprintLevel(((TileEntityDecorationController) entity).getBlueprintPath()) != -1))
            {
                structure = new BuildingStructureHandler<>(world,
                  position,
                  blueprint,
                  new PlacementSettings(isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotateTimes)),
                  this, new BuildingStructureHandler.Stage[] {BUILD_SOLID, WEAK_SOLID, CLEAR_WATER, CLEAR_NON_SOLIDS, DECORATE, SPAWN});
                building.setTotalStages(5);
            }
            else
            {
                structure = new BuildingStructureHandler<>(world,
                  position,
                  blueprint,
                  new PlacementSettings(isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotateTimes)),
                  this, new BuildingStructureHandler.Stage[] {CLEAR, BUILD_SOLID, WEAK_SOLID, CLEAR_WATER, CLEAR_NON_SOLIDS, DECORATE, SPAWN});
                building.setTotalStages(6);
            }

            job.setBlueprint(blueprint);
            job.getBlueprint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotateTimes), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, world);
            setStructurePlacer(structure);

            if (getProgressPos() != null)
            {
                structure.setStage(getProgressPos().getB());
            }
            this.loadingBlueprint = false;
        })));
    }

    /**
     * Set the structure placer.
     *
     * @param structure the placer.
     */
    public void setStructurePlacer(final BuildingStructureHandler<J, B> structure)
    {
        structurePlacer = new Tuple<>(new StructurePlacer(structure), structure);
    }

    /**
     * Check the placers inventory for the items in the itemList and remove it of the list if found.
     *
     * @param placer   the placer.
     * @param itemList the list to check.
     * @param force    if force insertion.
     * @return true if need to request.
     */
    public static <J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> ItemCheckResult hasListOfResInInvOrRequest(
      @NotNull final AbstractEntityAIStructure<J, B> placer,
      final List<ItemStack> itemList,
      final boolean force)
    {
        final Map<ItemStorage, Integer> requestedMap = new HashMap<>();
        for (final ItemStack stack : itemList)
        {
            if (stack.getItem() instanceof BlockItem && isBlockFree(((BlockItem) stack.getItem()).getBlock().defaultBlockState()))
            {
                continue;
            }

            ItemStorage tempStorage = new ItemStorage(stack.copy());
            if (requestedMap.containsKey(tempStorage))
            {
                final int oldSize = requestedMap.get(tempStorage);
                tempStorage.setAmount(tempStorage.getAmount() + oldSize);
            }
            requestedMap.put(tempStorage, tempStorage.getAmount());
        }

        for (final ItemStorage stack : requestedMap.keySet())
        {
            if (!InventoryUtils.hasItemInItemHandler(placer.getInventory(), stack1 -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack.getItemStack(), stack1))
                  && !placer.building.hasResourceInBucket(stack.getItemStack()))
            {
                return RECALC;
            }
        }
        // TODO: Why predicate based search when we got our ItemStorages we look for already?
        final List<ItemStack> foundStacks = InventoryUtils.filterItemHandler(placer.getWorker().getInventoryCitizen(),
          itemStack -> requestedMap.keySet().stream().anyMatch(storage -> ItemStackUtils.compareItemStacksIgnoreStackSize(storage.getItemStack(), itemStack)));

        final Map<ItemStorage, Integer> localMap = new HashMap<>();
        for (final ItemStack stack : foundStacks)
        {
            ItemStorage tempStorage = new ItemStorage(stack.copy());
            if (localMap.containsKey(tempStorage))
            {
                final int oldSize = localMap.get(tempStorage);
                tempStorage.setAmount(tempStorage.getAmount() + oldSize);
            }
            localMap.put(tempStorage, tempStorage.getAmount());
        }

        if (force)
        {
            for (final Map.Entry<ItemStorage, Integer> local : localMap.entrySet())
            {
                int req = requestedMap.getOrDefault(local.getKey(), 0);
                if (req != 0)
                {
                    if (local.getValue() >= req)
                    {
                        requestedMap.remove(local.getKey());
                    }
                    else
                    {
                        requestedMap.put(local.getKey(), req - local.getValue());
                    }
                }
            }
        }
        else
        {
            requestedMap.entrySet()
              .removeIf(entry -> ItemStackUtils.isEmpty(entry.getKey().getItemStack()) || foundStacks.stream()
                                                                                            .anyMatch(target -> ItemStackUtils.compareItemStacksIgnoreStackSize(target,
                                                                                              entry.getKey().getItemStack())));
        }

        for (final Map.Entry<ItemStorage, Integer> placedStack : requestedMap.entrySet())
        {
            final ItemStack stack = placedStack.getKey().getItemStack();
            if (ItemStackUtils.isEmpty(stack))
            {
                return FAIL;
            }

            final ImmutableList<IRequest<? extends IDeliverable>> requests = placer.building
                                                                               .getOpenRequestsOfTypeFiltered(
                                                                                 placer.getWorker().getCitizenData(),
                                                                                 TypeConstants.DELIVERABLE,
                                                                                 (IRequest<? extends IDeliverable> r) -> r.getRequest().matches(stack));

            final ImmutableList<IRequest<? extends IDeliverable>> completedRequests = placer.building
                                                                                        .getCompletedRequestsOfTypeFiltered(
                                                                                          placer.getWorker().getCitizenData(),
                                                                                          TypeConstants.DELIVERABLE,
                                                                                          (IRequest<? extends IDeliverable> r) -> r.getRequest().matches(stack));

            if (requests.isEmpty() && completedRequests.isEmpty())
            {
                final com.minecolonies.api.colony.requestsystem.requestable.Stack stackRequest = new Stack(stack, placer.getTotalAmount(stack).getCount(), 1);
                placer.getWorker().getCitizenData().createRequest(stackRequest);
                placer.registerBlockAsNeeded(stack);
                return FAIL;
            }
            else
            {
                for (final IRequest<? extends IDeliverable> request : requests)
                {
                    if (placer.worker.getCitizenJobHandler().getColonyJob().getAsyncRequests().contains(request.getId()))
                    {
                        placer.worker.getCitizenJobHandler().getColonyJob().markRequestSync(request.getId());
                    }
                }

                for (final IRequest<? extends IDeliverable> request : completedRequests)
                {
                    if (placer.worker.getCitizenJobHandler().getColonyJob().getAsyncRequests().contains(request.getId()))
                    {
                        placer.worker.getCitizenJobHandler().getColonyJob().markRequestSync(request.getId());
                    }
                }
            }
            return FAIL;
        }
        return SUCCESS;
    }

    /**
     * Load all requirements of the structure.
     *
     * @return the next state to go to.
     */
    public IAIState loadRequirements()
    {
        return START_WORKING;
    }

    /**
     * Iterates through all the required resources and stores them in the building.
     *
     * @return true if finished.
     */
    public boolean requestMaterials()
    {
        /*
         *  Override if needed.
         */
        return true;
    }

    /**
     * Register the block as needed at the building if possible.
     *
     * @param stack the stack.
     */
    public void registerBlockAsNeeded(final ItemStack stack)
    {
        /*
         * Override in child if possible.
         */
    }

    /**
     * Store the progressPos in the building if possible for the worker.
     *
     * @param blockPos the progressResult.
     * @param stage    the current stage.
     */
    public void storeProgressPos(final BlockPos blockPos, final BuildingStructureHandler.Stage stage)
    {
        /*
         * Override if needed.
         */
    }

    /**
     * Fill the list of the item positions to gather.
     */
    @Override
    public void fillItemsList()
    {
        if (!structurePlacer.getB().hasBluePrint())
        {
            return;
        }
        final Blueprint blueprint = structurePlacer.getB().getBluePrint();

        final BlockPos leftCorner = structurePlacer.getB().getWorldPos().subtract(blueprint.getPrimaryBlockOffset());
        searchForItems(new AABB(leftCorner, leftCorner.offset(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ())));
    }

    /**
     * Calculates the working position.
     * <p>
     * Takes a min distance from width and length.
     * <p>
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param targetPosition the position to work at.
     * @return BlockPos position to work from.
     */
    @Override
    public BlockPos getWorkingPosition(final BlockPos targetPosition)
    {
        //get length or width either is larger.
        final int length = structurePlacer.getB().getBluePrint().getSizeX();
        final int width = structurePlacer.getB().getBluePrint().getSizeZ();
        final int distance = Math.max(width, length) + MIN_ADDITIONAL_RANGE_TO_BUILD;

        return getWorkingPosition(distance, targetPosition, 0);
    }

    /**
     * Defines blocks that can be built for free.
     *
     * @param block The block to check if it is free.
     * @return true or false.
     */
    public static boolean isBlockFree(@Nullable final BlockState block)
    {
        return block == null
                 || BlockUtils.isWater(block)
                 || block.is(BlockTags.LEAVES)
                 || block.getBlock() == ModBlocks.blockDecorationPlaceholder;
    }

    /**
     * Let childs overwrite this if necessary.
     *
     * @return true if so.
     */
    protected boolean isAlreadyCleared()
    {
        return false;
    }

    /**
     * Get the current block being placed/removed. May be null if nothing is loaded yet.
     *
     * @return the current building position, or null.
     */
    @Nullable
    public BlockPos getCurrentBuildingPosition()
    {
        if (structurePlacer == null || structurePlacer.getA() == null || structurePlacer.getB() == null)
        {
            return null;
        }

        final BlockPos progressPos = structurePlacer.getA().getIterator().getProgressPos();
        if (progressPos == null || progressPos.equals(NULL_POS))
        {
            return null;
        }

        return structurePlacer.getB().getProgressPosInWorld(progressPos);
    }

    /**
     * Get the current working position for the worker. If workFrom is null calculate a new one.
     *
     * @return the current working position.
     */
    protected BlockPos getCurrentWorkingPosition()
    {
        return workFrom == null ? getWorkingPosition(getCurrentBuildingPosition()) : workFrom;
    }

    /**
     * Check if there is a StructureIterator to be build.
     *
     * @return true if we should start building.
     */
    protected boolean isThereAStructureToBuild()
    {
        if (structurePlacer == null || !structurePlacer.getB().hasBluePrint())
        {
            return false;
        }
        return true;
    }

    /**
     * Reduces the needed resources by 1.
     *
     * @param stack the stack which has been used now.
     */
    public void reduceNeededResources(final ItemStack stack)
    {
        /*
         * Nothing to be done here. Workers overwrite this if necessary.
         */
    }

    /**
     * Check for extra building options to do with each block.
     */
    public void checkForExtraBuildingActions()
    {
        /*
         * Override by worker if necessary.
         */
    }

    /**
     * Specific actions to handle a cancellation of a structure.
     */
    public void handleSpecificCancelActions()
    {
        /*
         * Child classes have to override this.
         */
    }

    /**
     * Check how much of a certain stuck is actually required.
     *
     * @param stack the stack to check.
     * @return the new stack with the correct amount.
     */
    @Nullable
    public ItemStack getTotalAmount(@Nullable final ItemStack stack)
    {
        return stack;
    }

    /**
     * Set the currentStructure to null.
     */
    public void resetCurrentStructure()
    {
        workFrom = null;
        structurePlacer = null;
        building.setProgressPos(null, null);
        worker.getCitizenData().setStatusPosition(null);
    }

    /**
     * Get the worker of the AI.
     *
     * @return the EntityCitizen object.
     */
    public AbstractEntityCitizen getWorker()
    {
        return this.worker;
    }

    /**
     * Get the current structure progress,
     *
     * @return the progress with the current stage.
     */
    public abstract Tuple<BlockPos, BuildingStructureHandler.Stage> getProgressPos();

    /**
     * Check if a solid substitution block should be overwritten in a specific case.
     *
     * @param worldBlock    the worldblock.
     * @param worldMetadata the world metadata.
     * @return true if should be overwritten.
     */
    public abstract boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata);

    /**
     * Searches a handy block to substitute a non-solid space which should be guaranteed solid.
     *
     * @param ignored the location the block should be at.
     * @return the Block.
     */
    public BlockState getSolidSubstitution(final BlockPos ignored)
    {
        return building.getSetting(FILL_BLOCK).getValue().getBlock().defaultBlockState();
    }

    /**
     * Execute specific actions on loading a structure.
     */
    protected abstract void executeSpecificCompleteActions();

    /**
     * Check if the structure tusk has been canceled.
     *
     * @return true if reset to idle.
     */
    protected abstract boolean checkIfCanceled();
}
