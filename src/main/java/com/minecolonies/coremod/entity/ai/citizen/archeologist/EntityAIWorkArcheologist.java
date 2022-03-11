package com.minecolonies.coremod.entity.ai.citizen.archeologist;

import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.GatePathResult;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.modules.ArcheologistsModule;
import com.minecolonies.coremod.colony.buildings.modules.RemoteAreaChunkLoadingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingArcheologist;
import com.minecolonies.coremod.colony.jobs.JobArcheologist;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.ColonyConstants.KEEP_LOADED_TYPE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_IDLING;

public class EntityAIWorkArcheologist extends AbstractEntityAIInteract<JobArcheologist, BuildingArcheologist>
{

    private final ResourceLocation WORKSPACE_STRUCTURE = new ResourceLocation(Constants.MOD_ID, "schematics/wooden/archeologist_workspace.blueprint");

    /**
     * Predicate defining things we don't want the archeologist to ever touch.
     */
    protected TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> DONT_TOUCH_PREDICATE = (info, worldPos, handler) ->
    {
        final BlockState worldState = handler.getWorld().getBlockState(worldPos);

        return worldState.getBlock() instanceof IBuilderUndestroyable
                 || worldState.getBlock() == Blocks.BEDROCK
                 || (info.getBlockInfo().getState().getBlock() instanceof AbstractBlockHut && handler.getWorldPos().equals(worldPos));
    };

    /**
     * The range in which the archeologists searches a gate.
     */
    private static final int SEARCH_RANGE = 1500;

    /**
     * Base xp gain for the composter.
     */
    private static final double BASE_XP_GAIN = 1;
    /**
     * The number of times the AI will check if the player has set any items on the list until messaging him
     */
    private static final int TICKS_UNTIL_COMPLAIN = 12000;
    /**
     * Number of ticks that the AI should wait before deciding again
     */
    private static final int DECIDE_DELAY = 40;
    /**
     * Number of ticks that the AI should wait after completing a task
     */
    private static final int AFTER_TASK_DELAY = 5;
    /**
     * The ticks elapsed since the last complain
     */
    private int ticksToComplain = 0;
    /**
     * The PathResult when the archeologist searches for a gate.
     */
    @Nullable
    private GatePathResult pathResult;

    /**
     * The Previous PathResult when the archeologist already found a gate.
     */
    @Nullable
    private GatePathResult lastPathResult;

    /**
     * Contains all resources needed for a certain build.
     */
    private final Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkArcheologist(@NotNull final JobArcheologist job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::decideWhatToDo, 1),
          new AITarget(ARCHEOLOGIST_DETERMINE_TARGET, this::determineTarget, 1),
          new AITarget(ARCHEOLOGIST_COLLECT_PAYMENT, this::collectPayment, 1),
          new AITarget(ARCHEOLOGIST_REQUEST_REQUIRED_RESOURCES, this::requestRequiredResources, 1),
          new AITarget(ARCHEOLOGIST_CLAIM_TARGET, this::claimTarget, 1),
          new AITarget(ARCHEOLOGIST_GOING_TO_GATE, this::getToGate, 1),
          new AITarget(ARCHEOLOGIST_SEARCHING_GATE, this::findGate, 1),
          new AITarget(ARCHEOLOGIST_TRAVELLING_TO_STRUCTURE, this::travelToStructure, 1),
          new AITarget(ARCHEOLOGIST_SPAWN_WORKSTATION, this::spawnWorkstation, 1),
          new AITarget(ARCHEOLOGIST_DO_WORK, this::doResearch, 1),
          new AITarget(ARCHEOLOGIST_CLEAR_WORKSTATION, this::clearWorkstation, 1),
          new AITarget(ARCHEOLOGIST_TRAVEL_HOME, this::travelHome, 1)
          );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Method for the AI to decide what to do. Possible actions: harvest barrels, fill barrels or idle
     *
     * @return the decision it made
     */
    private IAIState decideWhatToDo()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslatableComponent(COM_MINECOLONIES_COREMOD_STATUS_IDLING));
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);

        if (walkToBuilding())
        {
            setDelay(2);
            return getState();
        }

        if (getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget() == null)
        {
            return ARCHEOLOGIST_DETERMINE_TARGET;
        }

        setDelay(DECIDE_DELAY);
        return ARCHEOLOGIST_COLLECT_PAYMENT;
    }

    private IAIState collectPayment() {
        final BlockPos targetPosition = getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget();
        final int distance = (int) Math.sqrt(targetPosition.distSqr(getOwnBuilding().getPosition()));
        final int paymentInIngots = distance / 9;

        if (checkIfRequestForItemExistOrCreate(new ItemStack(Items.GOLD_INGOT, paymentInIngots))) {
            InventoryUtils.removeStackFromItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.GOLD_INGOT), paymentInIngots);
            return ARCHEOLOGIST_CLAIM_TARGET;
        };

        return ARCHEOLOGIST_COLLECT_PAYMENT;
    }

    protected IAIState claimTarget() {
        final BlockPos targetPosition = getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget();
        final ChunkPos chunkPos = new ChunkPos(targetPosition);

        if (world.getChunkSource() instanceof ServerChunkCache)
        {
            for (int x = chunkPos.x - 1; x < chunkPos.x + 1; x++)
            {
                for (int z = chunkPos.z - 1; z < chunkPos.z + 1; z++)
                {
                    final ChunkPos targetChunk = new ChunkPos(x, z);
                    getOwnBuilding().getFirstModuleOccurance(RemoteAreaChunkLoadingModule.class).addChunkToClaim(targetChunk);
                }
            }
        }

        setDelay(DECIDE_DELAY);
        return ARCHEOLOGIST_GOING_TO_GATE;
    }

    protected void removeTargetClaim()
    {
        final BlockPos targetPosition = getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget();
        final ChunkPos chunkPos = new ChunkPos(targetPosition);

        if (world.getChunkSource() instanceof ServerChunkCache)
        {
            for (int x = chunkPos.x - 1; x < chunkPos.x + 1; x++)
            {
                for (int z = chunkPos.z - 1; z < chunkPos.z + 1; z++)
                {
                    final ChunkPos targetChunk = new ChunkPos(x, z);
                    getOwnBuilding().getFirstModuleOccurance(RemoteAreaChunkLoadingModule.class).removeChunkToClaim(targetChunk);
                }
            }
        }
    }

    private IAIState requestRequiredResources() {
        final BlockPos targetPosition = getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget();

        final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(world, targetPosition, WORKSPACE_STRUCTURE.toString(), new PlacementSettings(), true);
        structure.getBluePrint().rotateWithMirror(Rotation.NONE, Mirror.NONE, world);
        StructurePlacer placer = new StructurePlacer(structure);
        StructurePhasePlacementResult result;
        BlockPos progressPos = NULL_POS;

        do
        {
            result = placer.executeStructureStep(world, null, progressPos, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
              () -> placer.getIterator().increment(DONT_TOUCH_PREDICATE.and((info, pos, handler) -> false)), true);

            progressPos = result.getIteratorPos();
            for (final ItemStack stack : result.getBlockResult().getRequiredItems())
            {
                addNeededResource(stack, stack.getCount());
            }
        }
        while (result.getBlockResult().getResult() != BlockPlacementResult.Result.FINISHED);

        if (checkIfRequestForItemStorageExistOrCreate(resources.values())) {
            InventoryUtils.removeStacksFromItemHandler(worker.getInventoryCitizen(), resources.values());
            return ARCHEOLOGIST_CLAIM_TARGET;
        };

        return ARCHEOLOGIST_REQUEST_REQUIRED_RESOURCES;
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
        final int hashCode = Objects.requireNonNull(res).hasTag() ? Objects.requireNonNull(res.getTag()).hashCode() : 0;
        final String key = res.getDescriptionId() + "-" + hashCode;
        ItemStorage resource = resources.get(key);
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        resources.put(key, resource);
    }

    /**
     * If the job class has no gate object the archeologist should search for one.
     *
     * @return the next IAIState the archeologist should switch to, after executing this method.
     */
    private IAIState getToGate()
    {
        if (job.getGate() == null)
        {
            return ARCHEOLOGIST_SEARCHING_GATE;
        }
        worker.getCitizenStatusHandler().setLatestStatus(new TranslatableComponent("com.minecolonies.coremod.status.goingtogate"));

        if (walkToGate())
        {
            return getState();
        }
        return ARCHEOLOGIST_TRAVELLING_TO_STRUCTURE;
    }

    /**
     * Uses the pathFinding system to search close gates.
     *
     * @return the next IAIState the archeologist should switch to, after executing this method
     */
    private IAIState findGate()
    {
        if (pathResult == null)
        {
            pathResult = worker.getNavigation().moveToGate(300, 1.0D);
            return getState();
        }
        if (pathResult.failedToReachDestination())
        {
            pathResult = null;
            return decideWhatToDo();
        }
        if (pathResult.isPathReachingDestination())
        {
            if (pathResult.gate != null)
            {
                job.setGate(new Tuple<>(pathResult.gate, pathResult.parent));
            }
            lastPathResult = pathResult;
            pathResult = null;
            return ARCHEOLOGIST_GOING_TO_GATE;
        }
        if (pathResult.isCancelled())
        {
            pathResult = null;
            return START_WORKING;
        }
        return getState();
    }

    /**
     * Triggers the travel to a target position once a gate has been reached.
     *
     * @return The next AI state.
     */
    private IAIState travelToStructure()
    {
        if (getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget() == null)
        {
            return START_WORKING;
        }

        //Check if we are traveling, we don't spawn an entity if we are traveling.
        if (worker.getCitizenData().getColony().getTravelingManager().isTravelling(worker.getCitizenData()))
        {
            return ARCHEOLOGIST_TRAVELLING_TO_STRUCTURE;
        }

        //Okey we are either just done traveling or the entity disappeared, lets check if we just finished traveling.
        final Optional<BlockPos> travelingTargetCandidate = worker.getCitizenData().getColony().getTravelingManager().getTravellingTargetFor(worker.getCitizenData());
        if (travelingTargetCandidate.isPresent())
        {
            //We just finished traveling, lets spawn the entity by setting the nextRespawnPosition.
            return ARCHEOLOGIST_SPAWN_WORKSTATION;
        }

        worker.getCitizenData().getColony().getTravelingManager().startTravellingTo(
          worker.getCitizenData(),
          getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget(),
          (int) Math.sqrt(getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).getTarget().distSqr(worker.blockPosition())) * 4
        );

        worker.remove(Entity.RemovalReason.DISCARDED);

        return ARCHEOLOGIST_TRAVELLING_TO_STRUCTURE;
    }

    private IAIState travelHome()
    {
        //Check if we are traveling, we don't spawn an entity if we are traveling.
        if (worker.getCitizenData().getColony().getTravelingManager().isTravelling(worker.getCitizenData()))
        {
            return ARCHEOLOGIST_TRAVEL_HOME;
        }

        //Okey we are either just done traveling or the entity disappeared, lets check if we just finished traveling.
        final Optional<BlockPos> travelingTargetCandidate = worker.getCitizenData().getColony().getTravelingManager().getTravellingTargetFor(worker.getCitizenData());
        if (travelingTargetCandidate.isPresent())
        {
            //We just finished traveling, lets spawn the entity by setting the nextRespawnPosition.
            getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).setTarget(null);
            return START_WORKING;
        }

        final BlockPos target = job.getGate() == null ? getOwnBuilding().getPosition() : job.getGate().getA();
        worker.getCitizenData().getColony().getTravelingManager().startTravellingTo(
          worker.getCitizenData(),
          target,
          (int) Math.sqrt(Objects.requireNonNull(target).distSqr(worker.blockPosition())) * 4
        );

        worker.remove(Entity.RemovalReason.DISCARDED);

        return ARCHEOLOGIST_TRAVEL_HOME;
    }

    /**
     * Let's the archeologist walk to the gate if the gate object in his job class already has been filled.
     *
     * @return true if the archeologist has arrived at the water.
     */
    private boolean walkToGate()
    {
        return job.getGate() != null && walkToBlock(Objects.requireNonNull(job.getGate().getA()), 0);
    }

    private IAIState determineTarget()
    {
        if (worker.level instanceof ServerLevel)
        {
            getOwnBuilding().getFirstModuleOccurance(ArcheologistsModule.class).setTarget(
              ModTags.archeologist_visitable.getValues()
                .stream()
                .map(feature -> {
                    final BlockPos candidatePos = ((ServerLevel) worker.level).findNearestMapFeature(feature, worker.blockPosition(), 100, false);
                    if (candidatePos != null)
                    {
                        final BlockPos surfacePos = worker.getLevel().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidatePos);
                        return determineSpawningCorner(surfacePos, feature);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(o -> worker.blockPosition().distSqr(o)))
                .orElse(null)
            );
        }

        setDelay(DECIDE_DELAY);
        return ARCHEOLOGIST_COLLECT_PAYMENT;
    }

    private BlockPos determineSpawningCorner(final BlockPos target, final StructureFeature<?> structureFeature) {
        final StructureStart<?> structureStart = getServerLevel().structureFeatureManager().getStructureAt(target, structureFeature);
        final BoundingBox structureBoundingBox = structureStart.getBoundingBox();

        final BlockPos center = structureBoundingBox.getCenter();
        final int width = structureBoundingBox.getXSpan();
        final int depth = structureBoundingBox.getZSpan();

        final int xOffset = (width / 2) + 3;
        final int zOffset = (depth / 2) + 3;

        final BlockPos[] corners = new BlockPos[]{
          center.mutable().move(-xOffset,0, -zOffset),
          center.mutable().move(-xOffset,0,  zOffset),
          center.mutable().move( xOffset,0,  zOffset),
          center.mutable().move( xOffset,0, -zOffset),
        };

        return Arrays.stream(corners)
                 .max(Comparator.comparingInt(o -> determineSpawningSize(o, center)))
                 .orElse(BlockPosUtil.expandAwayFromZero(target, 3));
    }

    private int determineSpawningSize(final BlockPos target, final BlockPos center) {
        final BlockPos fromCenterVector = target.subtract(center);
        final BlockPos scaledFromCenterVector = BlockPosUtil.expandAwayFromZero(fromCenterVector, 3);

        final BoundingBox boxToCheck = BoundingBox.fromCorners(fromCenterVector, scaledFromCenterVector);

        return (int) BlockPos.MutableBlockPos.betweenClosedStream(boxToCheck)
                 .filter(blockPos -> {
                     try {
                         return getServerLevel().getBlockState(blockPos).canBeReplaced(
                           new BlockPlaceContext(getLevel(),
                             null,
                             InteractionHand.MAIN_HAND,
                             ItemStack.EMPTY,
                             new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false)));
                     } catch (Exception ex) {
                         //What we do here is not really nice, with the empty stack and the weird click position, but hey we catch if it breaks.
                         return false;
                     }
                   }
                 )
                 .count();
    }

    @Override
    public Class<BuildingArcheologist> getExpectedBuildingClass()
    {
        return BuildingArcheologist.class;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }


    private IAIState spawnWorkstation()
    {
        return ARCHEOLOGIST_DO_WORK;
    }

    private IAIState doResearch()
    {
        return ARCHEOLOGIST_CLEAR_WORKSTATION;
    }

    private IAIState clearWorkstation()
    {
        return ARCHEOLOGIST_TRAVEL_HOME;
    }
}
