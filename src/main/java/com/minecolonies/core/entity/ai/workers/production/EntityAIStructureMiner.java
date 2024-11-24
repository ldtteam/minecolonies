package com.minecolonies.core.entity.ai.workers.production;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.MinerLevelManagementModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobMiner;
import com.minecolonies.core.colony.workorders.WorkOrderMiner;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.core.entity.ai.workers.util.BuildingStructureHandler;
import com.minecolonies.core.entity.ai.workers.util.MineNode;
import com.minecolonies.core.entity.ai.workers.util.MinerLevel;
import com.minecolonies.core.util.AdvancementUtils;
import com.minecolonies.core.util.WorkerUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.MORE_ORES;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.INVALID_MINESHAFT;
import static com.minecolonies.api.util.constant.TranslationConstants.NEEDS_BETTER_HUT;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner.FILL_BLOCK;
import static com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner.initStructure;
import static com.minecolonies.core.util.WorkerUtil.getLastLadder;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIStructureMiner extends AbstractEntityAIStructureWithWorkOrder<JobMiner, BuildingMiner>
{
    /**
     * The loot parameter set definition
     */
    public static final LootContextParamSet LUCKY_ORE_PARAM_SET = (new LootContextParamSet.Builder())
                                                                    .required(LootContextParams.ORIGIN)
                                                                    .required(LootContextParams.THIS_ENTITY)
                                                                    .required(LootContextParams.TOOL)
                                                                    .build();

    /**
     * Lucky ore loot table
     */
    public static final ResourceLocation LUCKY_ORE_LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "loot_tables/miner/lucky_ore");

    /**
     * Lead the miner to the other side of the shaft.
     */
    private static final int OTHER_SIDE_OF_SHAFT = 6;

    /**
     * Batchsizes of fill blocks to request.
     */
    private static final int COBBLE_REQUEST_BATCHES = 32;

    /**
     * Batch sizes of ladders to request.
     */
    private static final int LADDER_REQUEST_BATCHES = 10;

    public static final String RENDER_META_TORCH   = "torch";
    public static final String RENDER_META_STONE   = "stone";
    public static final String RENDER_META_SHOVEL  = "shovel";
    public static final String RENDER_META_PICKAXE = "pickaxe";

    private static final int NODE_DISTANCE    = 7;
    /**
     * Return to chest after building level stacks.
     */
    private static final int MAX_BLOCKS_MINED = 64;
    public static final  int SHAFT_RADIUS     = 3;
    private static final int SAFE_CHECK_RANGE = 5;

    /**
     * Considered the base of the shaft
     */
    private static final int SHAFT_BASE_DEPTH = 8;

    /**
     * Possible rotations.
     */
    private static final int ROTATE_ONCE        = 1;
    private static final int ROTATE_TWICE       = 2;
    private static final int ROTATE_THREE_TIMES = 3;
    private static final int ROTATE_FOUR_TIMES  = 4;

    /**
     * Check for liquids in the following range.
     */
    private static final int LIQUID_CHECK_RANGE = 5;

    /**
     * Mining icon
     */
    private final static VisibleCitizenStatus MINING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/miner.png"), "com.minecolonies.gui.visiblestatus.miner");

    //The current block to mine
    @Nullable
    private BlockPos minerWorkingLocation;

    //the last safe location now being air
    @Nullable
    private BlockPos currentStandingPosition;

    @Nullable
    private MineNode workingNode = null;

    /**
     * Constructor for the Miner. Defines the tasks the miner executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIStructureMiner(@NotNull final JobMiner job)
    {
        super(job);
        super.registerTargets(
          /*
           * If IDLE - switch to start working.
           */
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(PREPARING, MINER_CHECK_MINESHAFT, 1),
          new AITarget(MINER_WALKING_TO_LADDER, this::goToLadder, TICKS_SECOND),
          new AITarget(MINER_REPAIRING_LADDER, this::repairLadder, STANDARD_DELAY),
          new AITarget(MINER_CHECK_MINESHAFT, this::checkMineShaft, TICKS_SECOND),
          new AITarget(MINER_MINING_SHAFT, this::doShaftMining, STANDARD_DELAY),
          new AITarget(MINER_BUILDING_SHAFT, this::doShaftBuilding, STANDARD_DELAY),
          new AITarget(MINER_MINING_NODE, this::executeNodeMining, STANDARD_DELAY)
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
        if ((building.getLadderLocation() == null || worker.getY() >= building.getPosition().getY()) && walkToBuilding())
        {
            return START_WORKING;
        }

        if (building.getLadderLocation() == null || building.getCobbleLocation() == null)
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(INVALID_MINESHAFT), ChatPriority.BLOCKING));
            return START_WORKING;
        }

        if (!job.hasWorkOrder())
        {
            final List<WorkOrderMiner> list = building.getColony().getWorkManager().getOrderedList(WorkOrderMiner.class, building.getPosition());
            if (!list.isEmpty())
            {
                job.setWorkOrder(list.get(0));
                return LOAD_STRUCTURE;
            }
        }

        //Miner is at building
        return PREPARING;
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

        final BlockState blockState = world.getBlockState(blockToMine);
        if (!IColonyManager.getInstance().getCompatibilityManager().isOre(blockState))
        {
            blockToMine = getSurroundingOreOrDefault(blockToMine);
        }

        if (world.getBlockState(blockToMine).getBlock() instanceof AirBlock)
        {
            return BUILDING_STEP;
        }

        if (!mineBlock(blockToMine, getCurrentWorkingPosition()))
        {
            worker.swing(InteractionHand.MAIN_HAND);
            return getState();
        }

        blockToMine = getSurroundingOreOrDefault(blockToMine);
        if (IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(blockToMine)))
        {
            return getState();
        }

        worker.decreaseSaturationForContinuousAction();
        return BUILDING_STEP;
    }

    @Override
    public boolean shouldSilkTouchBlock(final BlockState curBlockState)
    {
        return IColonyManager.getInstance().getCompatibilityManager().isOre(curBlockState);
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
     * Walking to the ladder to check out the mine.
     *
     * @return next IAIState.
     */
    @NotNull
    private IAIState goToLadder()
    {
        if (walkToLadder())
        {
            return MINER_WALKING_TO_LADDER;
        }
        return MINER_REPAIRING_LADDER;
    }

    private boolean walkToLadder()
    {
        return walkToBlock(building.getLadderLocation());
    }

    @NotNull
    private IAIState repairLadder()
    {
        @NotNull final BlockPos nextCobble =
          new BlockPos(building.getCobbleLocation().getX(), getLastLadder(building.getLadderLocation(), world) - 1, building.getCobbleLocation().getZ());
        @NotNull final BlockPos nextLadder =
          new BlockPos(building.getLadderLocation().getX(), getLastLadder(building.getLadderLocation(), world) - 1, building.getLadderLocation().getZ());
        @NotNull final BlockPos safeStand =
          new BlockPos(building.getLadderLocation().getX(), getLastLadder(building.getLadderLocation(), world), building.getLadderLocation().getZ());

        if (!world.getBlockState(nextCobble).isSolid())
        {
            if (!checkIfRequestForItemExistOrCreate(new ItemStack(getSolidSubstitution(nextCobble).getBlock()), COBBLE_REQUEST_BATCHES, 1))
            {
                return getState();
            }
            if (!world.getBlockState(nextCobble).isAir() && !mineBlock(nextCobble, safeStand))
            {
                return getState();
            }
            setBlockFromInventory(nextCobble, getLadderBackFillBlock());
            return getState();
        }

        if (!world.getBlockState(nextLadder).isLadder(world, nextLadder, worker) && !world.getBlockState(nextLadder).isSolid())
        {
            if (!checkIfRequestForItemExistOrCreate(new ItemStack(Blocks.LADDER), LADDER_REQUEST_BATCHES, 1))
            {
                return getState();
            }
            if (!world.getBlockState(nextLadder).isAir() && !mineBlock(nextLadder, safeStand))
            {
                return getState();
            }
            //Get ladder orientation
            final BlockState metadata = Blocks.LADDER.defaultBlockState()
                                          .setValue(HorizontalDirectionalBlock.FACING,
                                            BlockPosUtil.directionFromDelta(nextLadder.getX() - nextCobble.getX(), 0, nextLadder.getZ() - nextCobble.getZ()));
            setBlockFromInventory(nextLadder, Blocks.LADDER, metadata);
            return getState();
        }
        return MINER_CHECK_MINESHAFT;
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

    /**
     * Get the ladderback fill block. Cobble for overworld, netherrack for nether.
     *
     * @return the ladderback fill block.
     */
    private Block getLadderBackFillBlock()
    {
        if (WorldUtil.isNetherType(world))
        {
            return Blocks.NETHERRACK;
        }
        return Blocks.COBBLESTONE;
    }

    @NotNull
    private IAIState checkMineShaft()
    {
        final BuildingMiner buildingMiner = building;
        // Check if we reached the bottom of the shaft
        if (getLastLadder(buildingMiner.getLadderLocation(), world) < world.getMinBuildHeight() + SHAFT_BASE_DEPTH)
        {
            AdvancementUtils.TriggerAdvancementPlayersForColony(job.getColony(), AdvancementTriggers.DEEP_MINE::trigger);
        }

        // Check if we reached the mineshaft depth limit
        if (getLastLadder(buildingMiner.getLadderLocation(), world) < buildingMiner.getDepthLimit(world))
        {
            //If the miner hut has been placed too deep.
            if (buildingMiner.getFirstModuleOccurance(MinerLevelManagementModule.class).getNumberOfLevels() == 0)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(NEEDS_BETTER_HUT), ChatPriority.BLOCKING));
                return IDLE;
            }
            worker.getCitizenData().setVisibleStatus(MINING);
            return MINER_MINING_NODE;
        }
        worker.getCitizenData().setVisibleStatus(MINING);
        return MINER_MINING_SHAFT;
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

    private IAIState doShaftMining()
    {

        minerWorkingLocation = getNextBlockInShaftToMine();
        if (minerWorkingLocation == null)
        {
            return advanceLadder(MINER_MINING_SHAFT);
        }

        //Note for future me:
        //we have to return; on false of this method
        //but omitted because end of method.
        if (mineBlock(minerWorkingLocation, currentStandingPosition))
        {
            worker.decreaseSaturationForContinuousAction();
        }

        return MINER_MINING_SHAFT;
    }

    private IAIState advanceLadder(final IAIState state)
    {
        if (!checkIfRequestForItemExistOrCreate(new ItemStack(getLadderBackFillBlock()), COBBLE_REQUEST_BATCHES, 1) ||
              !checkIfRequestForItemExistOrCreate(new ItemStack(Blocks.LADDER), LADDER_REQUEST_BATCHES, 1))
        {
            return state;
        }

        if (ladderDamaged())
        {
            return MINER_REPAIRING_LADDER;
        }

        final BlockPos vector = building.getLadderLocation().subtract(building.getCobbleLocation());
        final int xOffset = SHAFT_RADIUS * vector.getX();
        final int zOffset = SHAFT_RADIUS * vector.getZ();

        @NotNull final BlockPos nextLadder =
          new BlockPos(building.getLadderLocation().getX(), getLastLadder(building.getLadderLocation(), world) - 1, building.getLadderLocation().getZ());
        @NotNull final BlockPos safeCobble =
          new BlockPos(building.getLadderLocation().getX(), getLastLadder(building.getLadderLocation(), world) - 2, building.getLadderLocation().getZ());

        //Check for safe floor
        for (int x = -SAFE_CHECK_RANGE; x <= SAFE_CHECK_RANGE; x++)
        {
            for (int z = -SAFE_CHECK_RANGE; z <= SAFE_CHECK_RANGE; z++)
            {
                @NotNull final BlockPos curBlock = new BlockPos(safeCobble.getX() + x + xOffset, safeCobble.getY(), safeCobble.getZ() + z + zOffset);
                if (!secureBlock(curBlock, currentStandingPosition))
                {
                    return state;
                }
            }
        }

        @NotNull final BlockPos safeStand =
          new BlockPos(building.getLadderLocation().getX(), getLastLadder(building.getLadderLocation(), world), building.getLadderLocation().getZ());
        @NotNull final BlockPos nextCobble =
          new BlockPos(building.getCobbleLocation().getX(), getLastLadder(building.getLadderLocation(), world) - 1, building.getCobbleLocation().getZ());

        final MinerLevelManagementModule module = building.getFirstModuleOccurance(MinerLevelManagementModule.class);
        if (module.getStartingLevelShaft() == 0)
        {
            module.setStartingLevelShaft(nextCobble.getY() - 4);
        }

        if (nextCobble.getY() < module.getStartingLevelShaft())
        {
            return MINER_BUILDING_SHAFT;
        }

        if (!world.getBlockState(nextCobble).canBeReplaced() && (!mineBlock(nextCobble, safeStand))
              || (!world.getBlockState(nextLadder).canBeReplaced() && !mineBlock(nextLadder, safeStand)))
        {
            //waiting until blocks are mined
            return state;
        }


        //Get ladder orientation
        final BlockState metadata = getBlockState(safeStand);

        //set solid block
        setBlockFromInventory(nextCobble, getLadderBackFillBlock());
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.LADDER, metadata);
        this.incrementActionsDoneAndDecSaturation();
        return MINER_CHECK_MINESHAFT;
    }

    private BlockState getBlockState(@NotNull final BlockPos pos)
    {
        return world.getBlockState(pos);
    }

    /**
     * Calculates the next non-air block to mine. Will take the nearest block it finds.
     *
     * @return the next block to mine.
     */
    @Nullable
    private BlockPos getNextBlockInShaftToMine()
    {
        final BlockPos ladderPos = building.getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos, world);
        if (minerWorkingLocation == null)
        {
            minerWorkingLocation = new BlockPos(ladderPos.getX(), lastLadder + 1, ladderPos.getZ());
        }
        BlockState block = getBlockState(minerWorkingLocation);
        if (!block.isAir()
              && block.getBlock() != Blocks.LADDER
              && block.getFluidState().isEmpty())
        {
            if (currentStandingPosition == null)
            {
                currentStandingPosition = minerWorkingLocation;
            }
            return minerWorkingLocation;
        }
        currentStandingPosition = minerWorkingLocation;
        @Nullable BlockPos nextBlockToMine = null;
        double bestDistance = Double.MAX_VALUE;

        final BlockPos vector = building.getLadderLocation().subtract(building.getCobbleLocation());
        final int xOffset = SHAFT_RADIUS * vector.getX();
        final int zOffset = SHAFT_RADIUS * vector.getZ();

        //remove water for safety
        for (int x = SHAFT_RADIUS + xOffset + 2; x >= -SHAFT_RADIUS + xOffset - 2; x--)
        {
            for (int z = -SHAFT_RADIUS + zOffset - 2; z <= SHAFT_RADIUS + zOffset + 2; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                block = getBlockState(curBlock);
                if (!block.getFluidState().isEmpty())
                {
                    setBlockFromInventory(curBlock, getMainFillBlock());
                }
            }
        }


        //7x7 shaft find nearest block
        //Beware from positive to negative! to draw the miner to a wall to go down
        for (int x = SHAFT_RADIUS + xOffset; x >= -SHAFT_RADIUS + xOffset; x--)
        {
            for (int z = -SHAFT_RADIUS + zOffset; z <= SHAFT_RADIUS + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                final double distance = curBlock.distSqr(ladderPos) + Math.pow(curBlock.distSqr(minerWorkingLocation), 2);
                block = getBlockState(curBlock);
                if (distance < bestDistance
                      && !world.isEmptyBlock(curBlock))
                {
                    if (!block.getFluidState().isEmpty())
                    {
                        setBlockFromInventory(curBlock, getMainFillBlock());
                    }
                    nextBlockToMine = curBlock;
                    bestDistance = distance;
                }
            }
        }
        //find good looking standing position
        bestDistance = Double.MAX_VALUE;
        if (nextBlockToMine != null)
        {
            for (int x = 1; x >= -1; x--)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (x == 0 && 0 == z)
                    {
                        continue;
                    }
                    @NotNull final BlockPos curBlock = new BlockPos(nextBlockToMine.getX() + x, lastLadder, nextBlockToMine.getZ() + z);
                    final double distance = curBlock.distSqr(ladderPos);
                    if (distance < bestDistance && world.isEmptyBlock(curBlock))
                    {
                        currentStandingPosition = curBlock;
                        bestDistance = distance;
                    }
                }
            }
        }
        return nextBlockToMine;
    }

    @NotNull
    private IAIState doShaftBuilding()
    {
        if (walkToBuilding())
        {
            return MINER_BUILDING_SHAFT;
        }

        final BlockPos ladderPos = building.getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos, world) + 1;

        final BlockPos vector = ladderPos.subtract(building.getCobbleLocation());
        final int xOffset = SHAFT_RADIUS * vector.getX();
        final int zOffset = SHAFT_RADIUS * vector.getZ();

        initStructure(null, 0, new BlockPos(ladderPos.getX() + xOffset, lastLadder + 1, ladderPos.getZ() + zOffset), building, world, job);
        return LOAD_STRUCTURE;
    }

    @NotNull
    private IAIState executeNodeMining()
    {
        final MinerLevelManagementModule module = building.getFirstModuleOccurance(MinerLevelManagementModule.class);
        ;
        @Nullable final MinerLevel currentLevel = module.getCurrentLevel();
        if (currentLevel == null)
        {
            module.setCurrentLevel(module.getNumberOfLevels() - 1);
            return executeNodeMining();
        }
        return searchANodeToMine(currentLevel);
    }

    private IAIState searchANodeToMine(@NotNull final MinerLevel currentLevel)
    {
        final BuildingMiner buildingMiner = building;
        if (buildingMiner == null)
        {
            return IDLE;
        }

        final MinerLevelManagementModule module = building.getFirstModuleOccurance(MinerLevelManagementModule.class);
        ;
        if (workingNode == null || workingNode.getStatus() == MineNode.NodeStatus.COMPLETED)
        {
            workingNode = module.getActiveNode();
            module.setActiveNode(workingNode);

            if (workingNode == null)
            {
                final int levelId = module.getLevelId(currentLevel);
                if (levelId > 0)
                {
                    module.setCurrentLevel(levelId - 1);
                }
            }
            return MINER_CHECK_MINESHAFT;
        }

        //normal facing +x
        int rotation = 0;

        final int workingNodeX = workingNode.getX() > workingNode.getParent().getX() ? 1 : 0;
        final int workingNodeZ = workingNode.getZ() > workingNode.getParent().getZ() ? 1 : 0;
        final int vectorX = workingNode.getX() < workingNode.getParent().getX() ? -1 : workingNodeX;
        final int vectorZ = workingNode.getZ() < workingNode.getParent().getZ() ? -1 : workingNodeZ;

        if (vectorX == -1)
        {
            rotation = ROTATE_TWICE;
        }
        else if (vectorZ == -1)
        {
            rotation = ROTATE_THREE_TIMES;
        }
        else if (vectorZ == 1)
        {
            rotation = ROTATE_ONCE;
        }


        if (workingNode.getRot().isPresent() && workingNode.getRot().get() != rotation)
        {
            Log.getLogger().warn("Calculated rotation doesn't match recorded: x:" + workingNodeX + " z:" + workingNodeZ);
        }

        final MineNode parentNode = currentLevel.getNode(workingNode.getParent());

        if (parentNode != null && parentNode.getStyle() != MineNode.NodeType.SHAFT && parentNode.getStatus() != MineNode.NodeStatus.COMPLETED)
        {
            workingNode = parentNode;
            workingNode.setStatus(MineNode.NodeStatus.AVAILABLE);
            module.setActiveNode(parentNode);
            buildingMiner.markDirty();
            //We need to make sure to walk back to the last valid parent
            return MINER_CHECK_MINESHAFT;
        }
        @NotNull final BlockPos standingPosition = new BlockPos(workingNode.getParent().getX(), currentLevel.getDepth(), workingNode.getParent().getZ());
        currentStandingPosition = standingPosition;
        if (workingNode != null && currentLevel.getNode(new Vec2i(workingNode.getX(), workingNode.getZ())) == null)
        {
            module.setActiveNode(null);
            module.setOldNode(null);
            return MINER_MINING_SHAFT;
        }

        if ((workingNode.getStatus() == MineNode.NodeStatus.AVAILABLE || workingNode.getStatus() == MineNode.NodeStatus.IN_PROGRESS) && !walkToBlock(standingPosition))
        {
            workingNode.setRot(rotation);
            return executeStructurePlacement(workingNode, standingPosition, rotation);
        }
        return MINER_CHECK_MINESHAFT;
    }

    private boolean secureBlock(@NotNull final BlockPos curBlock, @NotNull final BlockPos safeStand)
    {
        final BlockState stateAtPos = getBlockState(curBlock);
        if ((!stateAtPos.blocksMotion() && getBlock(curBlock) != Blocks.TORCH) || !stateAtPos.getFluidState().isEmpty()
              || IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(curBlock)))
        {
            if (!mineBlock(curBlock, safeStand))
            {
                //make securing go fast to not confuse the player
                setDelay(1);
                return false;
            }
            if (!checkIfRequestForItemExistOrCreate(new ItemStack(getMainFillBlock()), COBBLE_REQUEST_BATCHES, 1))
            {
                return false;
            }

            setBlockFromInventory(curBlock, getMainFillBlock());
            //To set it to clean stone... would be cheating
            return false;
        }
        return true;
    }

    private IAIState executeStructurePlacement(@NotNull final MineNode mineNode, @NotNull final BlockPos standingPosition, final int rotation)
    {
        mineNode.setStatus(MineNode.NodeStatus.IN_PROGRESS);
        building.markDirty();
        //Preload structures
        if (job.getBlueprint() == null)
        {
            initStructure(mineNode,
              rotation,
              new BlockPos(mineNode.getX(), building.getFirstModuleOccurance(MinerLevelManagementModule.class).getCurrentLevel().getDepth(), mineNode.getZ()),
              building,
              world,
              job);
            return LOAD_STRUCTURE;
        }

        //Check for liquids
        for (int x = -NODE_DISTANCE / 2 - 1; x <= NODE_DISTANCE / 2 + 1; x++)
        {
            for (int z = -NODE_DISTANCE / 2 - 1; z <= NODE_DISTANCE / 2 + 1; z++)
            {
                for (int y = -1; y <= LIQUID_CHECK_RANGE; y++)
                {
                    @NotNull final BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    final BlockState block = getBlockState(curBlock);
                    if (block.getFluidState().isSource())
                    {
                        setBlockFromInventory(curBlock, getMainFillBlock());
                    }
                }
            }
        }

        workingNode = null;

        if (job.getBlueprint() != null)
        {
            return LOAD_STRUCTURE;
        }

        return MINER_MINING_NODE;
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

    private Block getBlock(@NotNull final BlockPos loc)
    {
        return world.getBlockState(loc).getBlock();
    }

    private int getFirstLadder(@NotNull BlockPos pos)
    {
        while (world.getBlockState(pos).isLadder(world, pos, worker))
        {
            pos = pos.above();
        }
        return pos.getY() - 1;
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        final BuildingMiner minerBuilding = building;
        //If shaft isn't cleared we're in shaft clearing mode.
        final MinerLevelManagementModule module = building.getFirstModuleOccurance(MinerLevelManagementModule.class);
        if (job.getBlueprint() != null)
        {
            if (job.getBlueprint().getName().contains("minermainshaft"))
            {
                final int depth = job.getWorkOrder().getLocation().getY();
                boolean exists = false;
                for (final MinerLevel level : module.getLevels())
                {
                    if (level.getDepth() == depth)
                    {
                        exists = true;
                        break;
                    }
                }

                @Nullable final BlockPos levelSignPos = WorkerUtil.findFirstLevelSign(job.getBlueprint(), job.getWorkOrder().getLocation());
                @NotNull final MinerLevel currentLevel = new MinerLevel(minerBuilding, job.getWorkOrder().getLocation().getY(), levelSignPos);
                if (!exists)
                {
                    module.addLevel(currentLevel);
                    module.setCurrentLevel(module.getNumberOfLevels());
                }
                WorkerUtil.updateLevelSign(world, currentLevel, module.getLevelId(currentLevel));
            }
            else
            {
                final MinerLevel currentLevel = module.getCurrentLevel();
                if (currentLevel == null)
                {
                    Log.getLogger().error("The mine state of the mine at: " + building.getID().toShortString() + " got corrupted. Trying to recover from this somehow....");

                    // This can only happen if something with the state got broken. Safest option is not handling the node closing and just doing the normal complete actions, it will potentially recover.
                }
                else
                {
                    currentLevel.closeNextNode(structurePlacer.getB().getSettings().rotation.ordinal(), module.getActiveNode(), world);
                    module.setActiveNode(null);
                    module.setOldNode(workingNode);
                    WorkerUtil.updateLevelSign(world, currentLevel, module.getLevelId(currentLevel));
                }
            }
        }
        super.executeSpecificCompleteActions();

        //Send out update to client
        building.markDirty();
        job.setBlueprint(null);
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
        return getNodeMiningPosition(targetPosition);
    }

    /**
     * Create a save mining position for the miner.
     *
     * @param blockToMine block which should be mined or placed.
     * @return the save position.
     */
    private BlockPos getNodeMiningPosition(final BlockPos blockToMine)
    {
        final BuildingMiner buildingMiner = building;
        final MinerLevelManagementModule module = buildingMiner.getFirstModuleOccurance(MinerLevelManagementModule.class);
        ;

        if (module.getCurrentLevel() == null || module.getActiveNode() == null)
        {
            return blockToMine;
        }
        final Vec2i parentPos = module.getActiveNode().getParent();
        final BlockPos vector = building.getLadderLocation().subtract(building.getCobbleLocation());

        if (parentPos != null && module.getCurrentLevel().getNode(parentPos) != null
              && module.getCurrentLevel().getNode(parentPos).getStyle() == MineNode.NodeType.SHAFT)
        {
            final BlockPos ladderPos = buildingMiner.getLadderLocation();
            return new BlockPos(
              ladderPos.getX() + vector.getX() * OTHER_SIDE_OF_SHAFT,
              module.getCurrentLevel().getDepth(),
              ladderPos.getZ() + vector.getZ() * OTHER_SIDE_OF_SHAFT);
        }
        final Vec2i pos = module.getActiveNode().getParent();
        return new BlockPos(pos.getX(), module.getCurrentLevel().getDepth(), pos.getZ());
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final BlockState worldMetadata)
    {
        return IColonyManager.getInstance().getCompatibilityManager().isOre(worldMetadata);
    }

    @Override
    protected void triggerMinedBlock(@NotNull final BlockPos position, @NotNull final BlockState blockToMine)
    {
        super.triggerMinedBlock(position, blockToMine);

        if (IColonyManager.getInstance().getCompatibilityManager().isLuckyBlock(blockToMine.getBlock()))
        {
            final double chance = 1 + worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(MORE_ORES);
            final boolean canGetLuckyBlock =
              worker.getRandom().nextDouble() * ONE_HUNDRED_PERCENT <= MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyBlockChance.get() * chance;

            if (canGetLuckyBlock)
            {
                final LootDataManager manager = building.getColony().getWorld().getServer().getLootData();
                final ResourceLocation lootTableId = LUCKY_ORE_LOOT_TABLE.withSuffix(String.valueOf(building.getBuildingLevel()));
                final LootParams lootParams = new Builder((ServerLevel) this.world)
                                                .withParameter(LootContextParams.ORIGIN, position.getCenter())
                                                .withParameter(LootContextParams.THIS_ENTITY, worker)
                                                .withParameter(LootContextParams.TOOL, worker.getMainHandItem())
                                                .create(LUCKY_ORE_PARAM_SET);

                final ObjectArrayList<ItemStack> randomItems = manager.getLootTable(lootTableId).getRandomItems(lootParams);
                for (final ItemStack stack : randomItems)
                {
                    InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(stack, worker.getInventoryCitizen());
                }
            }
        }

        if (IColonyManager.getInstance().getCompatibilityManager().isOre(blockToMine))
        {
            building.getColony().getStatisticsManager().increment(ORES_MINED, building.getColony().getDay());
        }
        building.getColony().getStatisticsManager().increment(BLOCKS_MINED, building.getColony().getDay());
    }

    @Override
    protected boolean checkIfCanceled()
    {
        if ((job.getWorkOrder() == null && job.getBlueprint() != null)
              || (structurePlacer != null && !structurePlacer.getB().hasBluePrint())
              || (job.getWorkOrder() != null && job.getWorkOrder().getStructurePath().contains("quarry")))
        {
            job.setBlueprint(null);
            if (job.hasWorkOrder())
            {
                job.getColony().getWorkManager().removeWorkOrder(job.getWorkOrderId());
            }
            job.setWorkOrder(null);
            resetCurrentStructure();
            building.cancelAllRequestsOfCitizen(worker.getCitizenData());
            building.setProgressPos(null, BuildingStructureHandler.Stage.CLEAR);
            return true;
        }

        if (!isThereAStructureToBuild())
        {
            switch ((AIWorkerState) getState())
            {
                case BUILDING_STEP:
                    return true;
                default:
                    return false;
            }
        }
        return job.getWorkOrder() != null && (!WorldUtil.isBlockLoaded(world, job.getWorkOrder().getLocation())) && getState() != PICK_UP_RESIDUALS;
    }

    private boolean ladderDamaged()
    {
        @NotNull final BlockPos nextLadder =
          new BlockPos(building.getLadderLocation().getX(), getLastLadder(building.getLadderLocation(), world) - 1, building.getLadderLocation().getZ());

        return !world.getBlockState(nextLadder).isLadder(world, nextLadder, worker) && !world.getBlockState(nextLadder).isSolid();
    }
}
