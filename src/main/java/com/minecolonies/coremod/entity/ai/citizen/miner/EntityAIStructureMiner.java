package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.TranslationTextComponent;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Vec2i;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuildMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructureWithWorkOrder;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.TranslationConstants.NEEDS_BETTER_HUT;
import static com.minecolonies.coremod.util.WorkerUtil.getLastLadder;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIStructureMiner extends AbstractEntityAIStructureWithWorkOrder<JobMiner>
{
    /**
     * Main shaft location.
     */
    private static final String MAIN_SHAFT_NAME = "/miner/minerMainShaft";

    /**
     * X4 shaft location.
     */
    private static final String X4_SHAFT_NAME = "/miner/minerX4";

    /**
     * X2 right shaft location.
     */
    private static final String X2_RIGHT_SHAFT_NAME = "/miner/minerX2Right";

    /**
     * X2 top shaft location.
     */
    private static final String X2_TOP_SHAFT_NAME = "/miner/minerX2Top";

    /**
     * Lead the miner to the other side of the shaft.
     */
    private static final int OTHER_SIDE_OF_SHAFT = 6;

    /**
     * Batchsizes of cobblestone to request.
     */
    private static final int COBBLE_REQUEST_BATCHES = 32;

    /**
     * Batch sizes of ladders to request.
     */
    private static final int LADDER_REQUEST_BATCHES = 10;

    private static final String RENDER_META_TORCH   = "Torch";
    private static final int    NODE_DISTANCE       = 7;
    /**
     * Return to chest after 3 stacks.
     */
    private static final int    MAX_BLOCKS_MINED    = 64;
    private static final int    LADDER_SEARCH_RANGE = 10;
    private static final int    SHAFT_RADIUS        = 3;
    private static final int    SAFE_CHECK_RANGE    = 5;

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

    //The current block to mine
    @Nullable
    private BlockPos minerWorkingLocation;

    //the last safe location now being air
    @Nullable
    private BlockPos currentStandingPosition;

    @Nullable
    private Node workingNode = null;

    /**
     * Constructor for the Miner.
     * Defines the tasks the miner executes.
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
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, 20),
          new AITarget(PREPARING, this::prepareForMining, 1),
          new AITarget(MINER_SEARCHING_LADDER, this::lookForLadder, 20),
          new AITarget(MINER_WALKING_TO_LADDER, this::goToLadder, 20),
          new AITarget(MINER_CHECK_MINESHAFT, this::checkMineShaft, 20),
          new AITarget(MINER_MINING_SHAFT, this::doShaftMining, STANDARD_DELAY),
          new AITarget(MINER_BUILDING_SHAFT, this::doShaftBuilding, STANDARD_DELAY),
          new AITarget(MINER_MINING_NODE, this::executeNodeMining, STANDARD_DELAY)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(
          2 * worker.getCitizenData().getStrength()
            + worker.getCitizenData().getEndurance());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingMiner.class;
    }

    //Miner wants to work but is not at building
    @NotNull
    private IAIState startWorkingAtOwnBuilding()
    {
        if (worker.getPosY() >= getOwnBuilding().getPosition().getY() && walkToBuilding())
        {
            return START_WORKING;
        }
        //Miner is at building
        return PREPARING;
    }

    @Override
    public BuildingMiner getOwnBuilding()
    {
        return getOwnBuilding(BuildingMiner.class);
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     * <p>
     * Override this to change the value.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return getOwnBuilding().getBuildingLevel() * MAX_BLOCKS_MINED;
    }

    @Override
    protected void updateRenderMetaData()
    {
        @NotNull final String renderMetaData = getRenderMetaTorch();
        //TODO: Have pickaxe etc. displayed?
        worker.setRenderMetadata(renderMetaData);
    }

    @NotNull
    private String getRenderMetaTorch()
    {
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Blocks.TORCH, -1))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    @NotNull
    private IAIState prepareForMining()
    {
        if (getOwnBuilding() != null && !getOwnBuilding().hasFoundLadder())
        {
            return MINER_SEARCHING_LADDER;
        }
        return MINER_CHECK_MINESHAFT;
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
        return MINER_CHECK_MINESHAFT;
    }

    private boolean walkToLadder()
    {
        return walkToBlock(getOwnBuilding().getLadderLocation());
    }

    @NotNull
    private IAIState checkMineShaft()
    {
        final BuildingMiner buildingMiner = getOwnBuilding();
        //Check if we reached the mineshaft depth limit
        if (getLastLadder(buildingMiner.getLadderLocation(), world) < buildingMiner.getDepthLimit())
        {
            //If the miner hut has been placed too deep.
            if (buildingMiner.getNumberOfLevels() == 0)
            {
                worker.getCitizenData().triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NEEDS_BETTER_HUT), ChatPriority.BLOCKING));
                buildingMiner.setClearedShaft(false);
                return IDLE;
            }
            buildingMiner.setClearedShaft(true);
            return MINER_MINING_NODE;
        }
        buildingMiner.setClearedShaft(false);
        return MINER_MINING_SHAFT;
    }

    @NotNull
    private IAIState lookForLadder()
    {
        @Nullable final BuildingMiner buildingMiner = getOwnBuilding();

        //Check for already found ladder
        if (buildingMiner.hasFoundLadder() && buildingMiner.getLadderLocation() != null)
        {
            if (world.getBlockState(buildingMiner.getLadderLocation()).getBlock() == Blocks.LADDER)
            {
                return MINER_WALKING_TO_LADDER;
            }
            else
            {
                buildingMiner.setFoundLadder(false);
                buildingMiner.setLadderLocation(null);
            }
        }

        final int posX = buildingMiner.getPosition().getX();
        final int posY = buildingMiner.getPosition().getY() + 2;
        final int posZ = buildingMiner.getPosition().getZ();
        for (int y = posY - LADDER_SEARCH_RANGE; y < posY; y++)
        {
            for (int x = posX - LADDER_SEARCH_RANGE; x < posX + LADDER_SEARCH_RANGE; x++)
            {
                for (int z = posZ - LADDER_SEARCH_RANGE; z < posZ + LADDER_SEARCH_RANGE; z++)
                {
                    tryFindLadderAt(new BlockPos(x, y, z));
                }
            }
        }

        return MINER_SEARCHING_LADDER;
    }

    private void tryFindLadderAt(@NotNull final BlockPos pos)
    {
        @Nullable final BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.hasFoundLadder())
        {
            return;
        }
        if (world.getBlockState(pos).getBlock().equals(Blocks.LADDER))
        {
            final int firstLadderY = getFirstLadder(pos);
            buildingMiner.setLadderLocation(new BlockPos(pos.getX(), firstLadderY, pos.getZ()));
            validateLadderOrientation();
        }
    }

    private void validateLadderOrientation()
    {
        @Nullable final BuildingMiner buildingMiner = getOwnBuilding();
        final EnumFacing ladderOrientation = world.getBlockState(buildingMiner.getLadderLocation()).getValue(BlockLadder.FACING);

        if (ladderOrientation == EnumFacing.WEST)
        {
            buildingMiner.setVectorX(-1);
            buildingMiner.setVectorZ(0);
        }
        else if (ladderOrientation == EnumFacing.EAST)
        {
            buildingMiner.setVectorX(1);
            buildingMiner.setVectorZ(0);
        }
        else if (ladderOrientation == EnumFacing.SOUTH)
        {
            buildingMiner.setVectorX(0);
            buildingMiner.setVectorZ(1);
        }
        else if (ladderOrientation == EnumFacing.NORTH)
        {
            buildingMiner.setVectorX(0);
            buildingMiner.setVectorZ(-1);
        }
        else
        {
            throw new IllegalStateException("Ladder metadata was " + ladderOrientation);
        }

        final int x = buildingMiner.getLadderLocation().getX();
        final int y = buildingMiner.getLadderLocation().getY();
        final int z = buildingMiner.getLadderLocation().getZ();

        buildingMiner.setCobbleLocation(new BlockPos(x - buildingMiner.getVectorX(), y, z - buildingMiner.getVectorZ()));
        buildingMiner.setShaftStart(new BlockPos(x, getLastLadder(buildingMiner.getLadderLocation(), world) - 1, z));
        buildingMiner.setFoundLadder(true);
    }

    private IAIState doShaftMining()
    {
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.mining"));

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

    @Override
    protected void triggerMinedBlock(@NotNull final IBlockState blockToMine)
    {
        super.triggerMinedBlock(blockToMine);
        if (IColonyManager.getInstance().getCompatibilityManager().isLuckyBlock(new ItemStack(blockToMine.getBlock())))
        {
            InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(IColonyManager.getInstance().getCompatibilityManager().getRandomLuckyOre(), new InvWrapper(worker.getInventoryCitizen()));
        }
    }

    private IAIState advanceLadder(final IAIState state)
    {
        if (getOwnBuilding().getStartingLevelShaft() > 4)
        {
            return MINER_BUILDING_SHAFT;
        }

        if (!checkIfRequestForItemExistOrCreate(new ItemStack(Blocks.COBBLESTONE, COBBLE_REQUEST_BATCHES), new ItemStack(Blocks.LADDER, LADDER_REQUEST_BATCHES)))
        {
            return state;
        }

        @NotNull final BlockPos safeCobble =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation(), world) - 2, getOwnBuilding().getLadderLocation().getZ());

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();
        //Check for safe floor
        for (int x = -SAFE_CHECK_RANGE + xOffset; x <= SAFE_CHECK_RANGE + xOffset; x++)
        {
            for (int z = -SAFE_CHECK_RANGE + zOffset; z <= SAFE_CHECK_RANGE + zOffset; z++)
            {
                @NotNull final BlockPos curBlock = new BlockPos(safeCobble.getX() + x, safeCobble.getY(), safeCobble.getZ() + z);
                if (!secureBlock(curBlock, currentStandingPosition))
                {
                    return state;
                }
            }
        }

        @NotNull final BlockPos safeStand =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation(), world), getOwnBuilding().getLadderLocation().getZ());
        @NotNull final BlockPos nextLadder =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation(), world) - 1, getOwnBuilding().getLadderLocation().getZ());
        @NotNull final BlockPos nextCobble =
          new BlockPos(getOwnBuilding().getCobbleLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation(), world) - 1, getOwnBuilding().getCobbleLocation().getZ());

        if (!mineBlock(nextCobble, safeStand) || !mineBlock(nextLadder, safeStand))
        {
            //waiting until blocks are mined
            return state;
        }


        //Get ladder orientation
        final IBlockState metadata = getBlockState(safeStand);

        //set cobblestone
        setBlockFromInventory(nextCobble, Blocks.COBBLESTONE);
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.LADDER, metadata);
        getOwnBuilding().incrementStartingLevelShaft();
        this.incrementActionsDoneAndDecSaturation();
        return MINER_CHECK_MINESHAFT;
    }

    private IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        return world.getBlockState(pos);
    }

    /**
     * Calculates the next non-air block to mine.
     * Will take the nearest block it finds.
     */
    @Nullable
    private BlockPos getNextBlockInShaftToMine()
    {

        final BlockPos ladderPos = getOwnBuilding().getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos, world);
        if (minerWorkingLocation == null)
        {
            minerWorkingLocation = new BlockPos(ladderPos.getX(), lastLadder + 1, ladderPos.getZ());
        }
        Block block = getBlock(minerWorkingLocation);
        if (block != null
              && block != Blocks.AIR
              && block != Blocks.LADDER
              && !(block.equals(Blocks.FLOWING_WATER)
                     || block.equals(Blocks.FLOWING_LAVA)))
        {
            return minerWorkingLocation;
        }
        currentStandingPosition = minerWorkingLocation;
        @Nullable BlockPos nextBlockToMine = null;
        double bestDistance = Double.MAX_VALUE;

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();

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
                block = getBlock(curBlock);
                if (block.equals(Blocks.WATER)
                      || block.equals(Blocks.LAVA)
                      || block.equals(Blocks.FLOWING_WATER)
                      || block.equals(Blocks.FLOWING_LAVA))
                {
                    setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
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
                final double distance = curBlock.distanceSq(ladderPos) + Math.pow(curBlock.distanceSq(minerWorkingLocation), 2);
                block = getBlock(curBlock);
                if (distance < bestDistance
                      && !world.isAirBlock(curBlock))
                {
                    if (block.equals(Blocks.WATER)
                          || block.equals(Blocks.LAVA)
                          || block.equals(Blocks.FLOWING_WATER)
                          || block.equals(Blocks.FLOWING_LAVA))
                    {
                        setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
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
                    final double distance = curBlock.distanceSq(ladderPos);
                    if (distance < bestDistance && world.isAirBlock(curBlock))
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

        final BlockPos ladderPos = getOwnBuilding().getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos, world) + 1;

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();

        initStructure(null, 0, new BlockPos(ladderPos.getX() + xOffset, lastLadder + 1, ladderPos.getZ() + zOffset));

        return CLEAR_STEP;
    }

    @NotNull
    private IAIState executeNodeMining()
    {
        @Nullable final Level currentLevel = getOwnBuilding().getCurrentLevel();
        if (currentLevel == null)
        {
            Log.getLogger().warn("Current Level not set, resetting...");
            getOwnBuilding().setCurrentLevel(getOwnBuilding().getNumberOfLevels() - 1);
            return executeNodeMining();
        }
        return searchANodeToMine(currentLevel);
    }

    private IAIState searchANodeToMine(@NotNull final Level currentLevel)
    {
        final BuildingMiner buildingMiner = getOwnBuilding(BuildingMiner.class);
        if (buildingMiner == null)
        {
            return IDLE;
        }

        if (workingNode == null || workingNode.getStatus() == Node.NodeStatus.COMPLETED)
        {
            workingNode = buildingMiner.getActiveNode();
            buildingMiner.setActiveNode(workingNode);
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

        final Node parentNode = currentLevel.getNode(workingNode.getParent());
        if (parentNode != null && parentNode.getStyle() != Node.NodeType.SHAFT && (parentNode.getStatus() != Node.NodeStatus.COMPLETED || world.getBlockState(new BlockPos(parentNode.getX(), currentLevel.getDepth() + 2, parentNode.getZ())).getBlock() != Blocks.AIR))
        {
            workingNode = parentNode;
            workingNode.setStatus(Node.NodeStatus.AVAILABLE);
            buildingMiner.setActiveNode(parentNode);
        }

        @NotNull final BlockPos standingPosition = new BlockPos(workingNode.getParent().getX(), currentLevel.getDepth(), workingNode.getParent().getZ());
        currentStandingPosition = standingPosition;
        if ((workingNode.getStatus() == Node.NodeStatus.AVAILABLE || workingNode.getStatus() == Node.NodeStatus.IN_PROGRESS) && !walkToBlock(standingPosition))
        {
            return executeStructurePlacement(workingNode, standingPosition, rotation);
        }
        return MINER_CHECK_MINESHAFT;
    }

    private boolean secureBlock(@NotNull final BlockPos curBlock, @NotNull final BlockPos safeStand)
    {
        if ((!getBlockState(curBlock).getMaterial().blocksMovement() && getBlock(curBlock) != Blocks.TORCH)
                || IColonyManager.getInstance().getCompatibilityManager().isOre(world.getBlockState(curBlock)))
        {
            if (!mineBlock(curBlock, safeStand))
            {
                //make securing go fast to not confuse the player
                setDelay(1);
                return false;
            }
            if (!checkIfRequestForItemExistOrCreate(new ItemStack(Blocks.COBBLESTONE, COBBLE_REQUEST_BATCHES)))
            {
                return false;
            }

            setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
            //To set it to clean stone... would be cheating
            return false;
        }
        return true;
    }

    /**
     * Initiates structure loading.
     *
     * @param mineNode     the node to load it for.
     * @param rotateTimes  The amount of time to rotate the structure.
     * @param structurePos The position of the structure.
     */
    private void initStructure(final Node mineNode, final int rotateTimes, final BlockPos structurePos)
    {
        final String style = getOwnBuilding().getStyle();
        String requiredName = null;
        int rotateCount = 0;

        if (mineNode == null)
        {
            rotateCount = getRotationFromVector();
            requiredName = getCorrectStyleLocation(style, MAIN_SHAFT_NAME);
        }
        else
        {
            rotateCount = rotateTimes;
            if (mineNode.getStyle() == Node.NodeType.CROSSROAD)
            {
                requiredName = getCorrectStyleLocation(style, X4_SHAFT_NAME);
            }
            else if (mineNode.getStyle() == Node.NodeType.BEND)
            {
                requiredName = getCorrectStyleLocation(style, X2_RIGHT_SHAFT_NAME);
            }
            else if (mineNode.getStyle() == Node.NodeType.TUNNEL)
            {
                requiredName = getCorrectStyleLocation(style, X2_TOP_SHAFT_NAME);
            }
        }

        if (requiredName != null)
        {
            if (job.getWorkOrder() == null)
            {
                final WorkOrderBuildMiner wo = new WorkOrderBuildMiner(requiredName, requiredName, rotateCount, structurePos, false, getOwnBuilding().getPosition());
                worker.getCitizenColonyHandler().getColony().getWorkManager().addWorkOrder(wo, false);
                job.setWorkOrder(wo);
                initiate();
            }
            else if (currentStructure == null)
            {
                initiate();
            }
        }
    }

    /**
     * Get the correct style for the shaft. Return default back.
     * @param style the style to check.
     * @param shaft the shaft.
     * @return the correct location.
     */
    private String getCorrectStyleLocation(final String style, final String shaft)
    {
        final Structure wrapper = new Structure(world, Structures.SCHEMATICS_PREFIX + "/" + style + shaft, new PlacementSettings());
        if (wrapper.getBluePrint() != null)
        {
            return Structures.SCHEMATICS_PREFIX + "/" + style + shaft;
        }
        else
        {
            return Structures.SCHEMATICS_PREFIX + shaft;
        }
    }

    /**
     * Return number of rotation for our building, for the main shaft.
     *
     * @return the rotation.
     */
    private int getRotationFromVector()
    {
        if (getOwnBuilding().getVectorX() == 1)
        {
            return ROTATE_ONCE;
        }
        else if (getOwnBuilding().getVectorZ() == 1)
        {
            return ROTATE_TWICE;
        }
        else if (getOwnBuilding().getVectorX() == -1)
        {
            return ROTATE_THREE_TIMES;
        }
        else if (getOwnBuilding().getVectorZ() == -1)
        {
            return ROTATE_FOUR_TIMES;
        }
        return 0;
    }

    private IAIState executeStructurePlacement(@NotNull final Node mineNode, @NotNull final BlockPos standingPosition, final int rotation)
    {
        mineNode.setStatus(Node.NodeStatus.IN_PROGRESS);
        //Preload structures
        if (job.getStructure() == null)
        {
            initStructure(mineNode, rotation, new BlockPos(mineNode.getX(), getOwnBuilding().getCurrentLevel().getDepth(), mineNode.getZ()));
        }

        //Check for liquids
        for (int x = -NODE_DISTANCE / 2 - 1; x <= NODE_DISTANCE / 2 + 1; x++)
        {
            for (int z = -NODE_DISTANCE / 2 - 1; z <= NODE_DISTANCE / 2 + 1; z++)
            {
                for (int y = -1; y <= LIQUID_CHECK_RANGE; y++)
                {
                    @NotNull final BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    final Block block = getBlock(curBlock);
                    if (block.equals(Blocks.WATER)
                          || block.equals(Blocks.LAVA)
                          || block.equals(Blocks.FLOWING_WATER)
                          || block.equals(Blocks.FLOWING_LAVA))
                    {
                        setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
                    }
                }
            }
        }

        workingNode = null;

        if (job.getStructure() != null)
        {
            onStartWithoutStructure();
            return CLEAR_STEP;
        }

        return MINER_MINING_NODE;
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, @NotNull final Block block)
    {
        worker.swingArm(worker.getActiveHand());
        setBlockFromInventory(location, block, block.getDefaultState());
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, final Block block, final IBlockState metadata)
    {
        final int slot;
        if (block instanceof BlockLadder)
        {
            slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block, -1);
        }
        else
        {
            slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(block, block.getMetaFromState(metadata));
        }
        if (slot != -1)
        {
            new InvWrapper(getInventory()).extractItem(slot, 1, false);
            //Flag 1+2 is needed for updates
            world.setBlockState(location, metadata, 0x03);
        }
    }

    private Block getBlock(@NotNull final BlockPos loc)
    {
        return world.getBlockState(loc).getBlock();
    }

    private int getFirstLadder(@NotNull final BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, worker))
        {
            return getFirstLadder(pos.up());
        }
        else
        {
            return pos.getY() - 1;
        }
    }

    @Override
    public void executeSpecificCompleteActions()
    {
        final BuildingMiner minerBuilding = getOwnBuilding();
        //If shaft isn't cleared we're in shaft clearing mode.
        if (minerBuilding.hasClearedShaft())
        {
            final Level currentLevel = minerBuilding.getCurrentLevel();

            currentLevel.closeNextNode(getRotation(), getOwnBuilding().getActiveNode());
            getOwnBuilding(BuildingMiner.class).setActiveNode(null);
            getOwnBuilding(BuildingMiner.class).setOldNode(workingNode);
            WorkerUtil.updateLevelSign(world, currentLevel, minerBuilding.getLevelId(currentLevel));
        }
        else if (job.getStructure() != null)
        {
            @Nullable final BlockPos levelSignPos = WorkerUtil.findFirstLevelSign(job.getStructure());
            @NotNull final Level currentLevel = new Level(minerBuilding, job.getStructure().getPosition().getY(), levelSignPos);

            minerBuilding.addLevel(currentLevel);
            minerBuilding.setCurrentLevel(minerBuilding.getNumberOfLevels());
            minerBuilding.resetStartingLevelShaft();
            WorkerUtil.updateLevelSign(world, currentLevel, minerBuilding.getLevelId(currentLevel));
        }
        super.executeSpecificCompleteActions();

        //Send out update to client
        getOwnBuilding().markDirty();
        job.setStructure(null);

        final IColony colony = worker.getCitizenColonyHandler().getColony();
        if (colony != null)
        {
            final List<WorkOrderBuildMiner> workOrders = colony.getWorkManager().getWorkOrdersOfType(WorkOrderBuildMiner.class);
            if (workOrders.size() > 2)
            {
                for (WorkOrderBuildMiner order : workOrders)
                {
                    if (this.getOwnBuilding().getID().equals(order.getMinerBuilding()))
                    {
                        colony.getWorkManager().removeWorkOrder(order.getID());
                    }
                }
            }
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
        final BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.getCurrentLevel() == null || buildingMiner.getActiveNode() == null)
        {
            return blockToMine;
        }
        final Vec2i parentPos = buildingMiner.getActiveNode().getParent();
        if (parentPos != null && buildingMiner.getCurrentLevel().getNode(parentPos) != null
              && buildingMiner.getCurrentLevel().getNode(parentPos).getStyle() == Node.NodeType.SHAFT)
        {
            final BlockPos ladderPos = buildingMiner.getLadderLocation();
            return new BlockPos(
                                 ladderPos.getX() + buildingMiner.getVectorX() * OTHER_SIDE_OF_SHAFT,
                                 buildingMiner.getCurrentLevel().getDepth(),
                                 ladderPos.getZ() + buildingMiner.getVectorZ() * OTHER_SIDE_OF_SHAFT);
        }
        final Vec2i pos = buildingMiner.getActiveNode().getParent();
        return new BlockPos(pos.getX(), buildingMiner.getCurrentLevel().getDepth(), pos.getZ());
    }

    @Override
    public boolean shallReplaceSolidSubstitutionBlock(final Block worldBlock, final IBlockState worldMetadata)
    {
        return IColonyManager.getInstance().getCompatibilityManager().isOre(worldMetadata);
    }

    @Override
    public IBlockState getSolidSubstitution(final BlockPos ignored)
    {
        return Blocks.COBBLESTONE.getDefaultState();
    }

    @Override
    protected boolean checkIfCanceled()
    {
        if (super.checkIfCanceled())
        {
            return true;
        }
        if (!isThereAStructureToBuild())
        {
            switch ((AIWorkerState) getState())
            {
                case CLEAR_STEP:
                case BUILDING_STEP:
                case DECORATION_STEP:
                case SPAWN_STEP:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }
}
