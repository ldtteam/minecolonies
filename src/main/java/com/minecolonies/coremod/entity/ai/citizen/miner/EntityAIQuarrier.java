package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructureIterators;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.SurfaceType;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.modules.QuarryModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobQuarrier;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler;
import com.minecolonies.coremod.entity.ai.util.WorkerLoadOnlyStructureHandler;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.BLOCK_PLACE_SPEED;
import static com.minecolonies.api.research.util.ResearchConstants.MORE_ORES;
import static com.minecolonies.api.util.constant.CitizenConstants.PROGRESS_MULTIPLIER;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner.FILL_BLOCK;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure.ItemCheckResult.RECALC;
import static com.minecolonies.coremod.entity.ai.util.BuildingStructureHandler.Stage.*;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIQuarrier extends AbstractEntityAIStructureWithWorkOrder<JobQuarrier, BuildingMiner>
{
    private static final String RENDER_META_TORCH = "torch";
    private static final String RENDER_META_STONE = "stone";

    /**
     * Return to chest after 3 stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;

    /**
     * Mining icon
     */
    private final static VisibleCitizenStatus MINING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/miner.png"), "com.minecolonies.gui.visiblestatus.miner");

    /**
     * Constructor for the Miner. Defines the tasks the miner executes.
     *
     * @param job a fisherman job to use.
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
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(QUARRY_MINER_NO_QUARRY), ChatPriority.BLOCKING));
            return IDLE;
        }

        if (quarry.getFirstModuleOccurance(QuarryModule.class).isFinished())
        {
            walkToBuilding();
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(QUARRY_MINER_FINISHED_QUARRY), ChatPriority.BLOCKING));
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

            final String name = Structures.SCHEMATICS_PREFIX + "/" + quarry.getStyle() + "/" + quarry.getSchematicName() + "shaft1";
            final WorkOrderBuildMiner wo = new WorkOrderBuildMiner(name, name, quarry.getRotation(), quarry.getPosition().below(2), false, getOwnBuilding().getPosition());
            getOwnBuilding().getColony().getWorkManager().addWorkOrder(wo, false);
            job.setWorkOrder(wo);
        }

        return super.loadRequirements();
    }

    @Override
    public void loadStructure(@NotNull final String name, final int rotateTimes, final BlockPos position, final boolean isMirrored, final boolean removal)
    {
        final BuildingStructureHandler<JobQuarrier, BuildingMiner> structure;

        structure = new BuildingStructureHandler<>(world,
          position,
              name,
              new PlacementSettings(isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, BlockPosUtil.getRotationFromRotations(rotateTimes)),
              this, new BuildingStructureHandler.Stage[] {BUILD_SOLID, DECORATE, CLEAR});
            getOwnBuilding().setTotalStages(3);


        if (!structure.hasBluePrint())
        {
            handleSpecificCancelActions();
            Log.getLogger().warn("Couldn't find structure with name: " + name + " aborting loading procedure");
            return;
        }

        job.setBlueprint(structure.getBluePrint());
        job.getBlueprint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotateTimes), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, world);
        setStructurePlacer(structure);

        if (getProgressPos() != null)
        {
            structure.setStage(getProgressPos().getB());
        }
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

        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.building"));

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
                    .decrement(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !info.getBlockInfo().getState().getMaterial().isSolid() || isDecoItem(info.getBlockInfo()
                      .getState()
                      .getBlock()))),
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
                    .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> info.getBlockInfo().getState().getMaterial().isSolid() && !isDecoItem(info.getBlockInfo()
                      .getState()
                      .getBlock()))),
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
                    getOwnBuilding().nextStage();
                    getOwnBuilding().setProgressPos(null, null);
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

            //todo adjust in 1.18 to actual world height.
            if (currentWorldPos.getY() < 5)
            {
                getOwnBuilding().setProgressPos(null, null);
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
            final AbstractBuildingStructureBuilder buildingWorker = getOwnBuilding();
            buildingWorker.resetNeededResources();
            requestProgress = NULL_POS;
            requestState = RequestStage.SOLID;
        }

        final BlockPos worldPos = structure.getProgressPosInWorld(requestProgress);


        //todo double check this here
        final RequestStage currState = requestState;
        switch (currState)
        {
            case SOLID:
                result = placer.executeStructureStep(world,
                  null,
                  requestProgress,
                  StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator()
                    .decrement(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> !info.getBlockInfo().getState().getMaterial().isSolid() || isDecoItem(info.getBlockInfo()
                      .getState()
                      .getBlock())  || pos.getY()  < worldPos.getY())),
                  false);

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    getOwnBuilding().addNeededResource(stack, stack.getCount());
                }

                if (requestProgress.getY() != -1 && result.getIteratorPos().getY() < requestProgress.getY())
                {
                    requestProgress = new BlockPos(0, requestProgress.getY() + 1, 0);
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
                    .increment(DONT_TOUCH_PREDICATE.or((info, pos, handler) -> info.getBlockInfo().getState().getMaterial().isSolid() && !isDecoItem(info.getBlockInfo()
                      .getState()
                      .getBlock())  || pos.getY() > worldPos.getY())),
                  false);

                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    getOwnBuilding().addNeededResource(stack, stack.getCount());
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
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(QUARRY_MINER_NO_QUARRY), ChatPriority.BLOCKING));
            return true;
        }
        else if (job.findQuarry().getFirstModuleOccurance(QuarryModule.class).isFinished())
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(QUARRY_MINER_FINISHED_QUARRY), ChatPriority.BLOCKING));
            return true;
        }
        else if(job.getWorkOrder() != null && !job.getWorkOrder().getSchematicLocation().equals(job.findQuarry().getPosition().below(2)))
        {
            blockToMine = null;
            job.complete();
            getOwnBuilding().setProgressPos(null, null);
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
        return getOwnBuilding().getBuildingLevel() * MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata(getRenderMetaStone() + getRenderMetaTorch());
    }

    /**
     * Get render data to render torches at the backpack if in inventory.
     *
     * @return metaData String if so.
     */
    @NotNull
    private String getRenderMetaTorch()
    {
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.TORCH))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    /**
     * Get render data to render stone in the backpack if cobble in inventory.
     *
     * @return metaData String if so.
     */
    @NotNull
    private String getRenderMetaStone()
    {
        if (worker.getCitizenInventoryHandler().hasItemInInventory(getMainFillBlock()))
        {
            return RENDER_META_STONE;
        }
        return "";
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

        final BlockState blockState = world.getBlockState(blockToMine);
        if (!IColonyManager.getInstance().getCompatibilityManager().isOre(blockState))
        {
            blockToMine = getSurroundingOreOrDefault(blockToMine);
        }

        if (world.getBlockState(blockToMine).getBlock() instanceof AirBlock)
        {
            blockToMine = null;
            return BUILDING_STEP;
        }

        if (!mineBlock(blockToMine, getCurrentWorkingPosition()))
        {
            worker.swing(Hand.MAIN_HAND);
            return getState();
        }

        blockToMine = getSurroundingOreOrDefault(blockToMine);
        if (IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)))
        {
            return getState();
        }

        worker.decreaseSaturationForContinuousAction();
        blockToMine  = null;
        return BUILDING_STEP;
    }

    private BlockPos getSurroundingOreOrDefault(final BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            final BlockPos offset = pos.relative(direction);
            if (IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(offset)))
            {
                return offset;
            }
        }
        return pos;
    }

    /**
     * Get the main fill block. Based on the settings.
     *
     * @return the main fill block.
     */
    private Block getMainFillBlock()
    {
        return getOwnBuilding().getSetting(FILL_BLOCK).getValue().getBlock();
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

    private void setBlockFromInventory(@NotNull final BlockPos location, @NotNull final Block block)
    {
        worker.swing(worker.getUsedItemHand());
        setBlockFromInventory(location, block, block.defaultBlockState());
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, final Block block, final BlockState metadata)
    {
        final int slot;
        if (block instanceof LadderBlock)
        {
            slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block);
        }
        else
        {
            slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block);
        }
        if (slot != -1)
        {
            getInventory().extractItem(slot, 1, false);
            //Flag 1+2 is needed for updates
            WorldUtil.setBlockState(world, location, metadata);
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
}
