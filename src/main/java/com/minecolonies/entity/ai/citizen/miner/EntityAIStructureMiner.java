package com.minecolonies.entity.ai.citizen.miner;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.entity.ai.item.handling.ItemStorage;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.Log;
import com.minecolonies.util.StructureWrapper;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Class which handles the miner behaviour.
 */
public class EntityAIStructureMiner extends AbstractEntityAIStructure<JobMiner>
{

    private static final String     RENDER_META_TORCH         = "Torch";
    private static final int        NODE_DISTANCE             = 7;
    /**
     * Return to chest after 3 stacks.
     */
    private static final int        MAX_BLOCKS_MINED          = 3 * 64;
    /*
    Blocks that will be ignored while building shaft/node walls and are certainly safe.
     */
    private static final Set<Block> notReplacedInSecuringMine = new HashSet<>(Arrays.asList(Blocks.COBBLESTONE, Blocks.STONE, Blocks.DIRT));
    private static final int        DELAY_TIMEOUT             = 10;
    private static final int        LADDER_SEARCH_RANGE       = 10;
    private static final int        SHAFT_RADIUS              = 3;
    private static final int        SAFE_CHECK_RANGE          = 5;
    private static final int        SAFE_CHECK_UPPER_BOUND    = 4;
    private static final int        SAFE_CHECK_LOWER_BOUND    = -7;

    /**
     * Amount of items to be kept.
     */
    private static final int STACK_MAX_SIZE = 64;

    //The current block to mine
    @Nullable
    private BlockPos currentWorkingLocation;
    //the last safe location now being air
    @Nullable
    private BlockPos currentStandingPosition;
    @Nullable
    private Node    workingNode    = null;
    private boolean requestedBlock = false;
    private boolean buildStructure = false;

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
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForMining),
          new AITarget(MINER_SEARCHING_LADDER, this::lookForLadder),
          new AITarget(MINER_WALKING_TO_LADDER, this::goToLadder),
          new AITarget(MINER_CHECK_MINESHAFT, this::checkMineShaft),
          new AITarget(MINER_MINING_SHAFT, this::doShaftMining),
          new AITarget(MINER_BUILDING_SHAFT, this::doShaftBuilding),
          new AITarget(MINER_MINING_NODE, this::doNodeMining)
        );
        worker.setSkillModifier(
          2 * worker.getCitizenData().getStrength()
            + worker.getCitizenData().getEndurance());
        worker.setCanPickUpLoot(true);
    }

    //Miner wants to work but is not at building
    @NotNull
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return START_WORKING;
        }
        //Miner is at building
        return PREPARING;
    }

    @NotNull
    private AIState prepareForMining()
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
     * @return next AIState.
     */
    @NotNull
    private AIState goToLadder()
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
        return MAX_BLOCKS_MINED;
    }

    @Override
    protected BuildingMiner getOwnBuilding()
    {
        return (BuildingMiner) worker.getWorkBuilding();
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
        if (worker.hasItemInInventory(Blocks.TORCH))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    /**
     * Override this method if you want to keep an amount of items in inventory.
     * When the inventory is full, everything get's dumped into the building chest.
     * But you can use this method to hold some stacks back.
     *
     * @return a list of objects which should be kept.
     */
    @Override
    protected Map<ItemStorage, Integer> needXForWorker()
    {
        final Map<ItemStorage, Integer> keepX = new HashMap<>();
        final ItemStack stackLadder = new ItemStack(Blocks.LADDER);
        final ItemStack stackFence = new ItemStack(Blocks.OAK_FENCE);
        final ItemStack stackTorch = new ItemStack(Blocks.TORCH);
        final ItemStack stackCobble = new ItemStack(Blocks.COBBLESTONE);
        final ItemStack stackSlab = new ItemStack(Blocks.WOODEN_SLAB);
        final ItemStack stackPlanks = new ItemStack(Blocks.PLANKS);

        keepX.put(new ItemStorage(stackLadder.getItem(), stackLadder.getItemDamage(), 0, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackFence.getItem(), stackFence.getItemDamage(), 0, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackTorch.getItem(), stackTorch.getItemDamage(), 0, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackCobble.getItem(), stackCobble.getItemDamage(), 0, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackSlab.getItem(), stackSlab.getItemDamage(), 0, false), STACK_MAX_SIZE);
        keepX.put(new ItemStorage(stackPlanks.getItem(), stackPlanks.getItemDamage(), 0, false), STACK_MAX_SIZE);

        return keepX;
    }

    @Override
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }

    @NotNull
    private AIState checkMineShaft()
    {
        //TODO: check if mineshaft needs repairing!
        //Check if we reached the mineshaft depth limit
        if (getLastLadder(getOwnBuilding().getLadderLocation()) < getOwnBuilding().getDepthLimit())
        {
            //If the miner hut has been placed too deep.
            if (getOwnBuilding().getNumberOfLevels() == 0)
            {
                chatSpamFilter.talkWithoutSpam("entity.miner.messageRequiresBetterHut");
                getOwnBuilding().clearedShaft = false;
                return IDLE;
            }
            getOwnBuilding().clearedShaft = true;
            return MINER_MINING_NODE;
        }
        getOwnBuilding().clearedShaft = false;
        return MINER_MINING_SHAFT;
    }

    @NotNull
    private AIState lookForLadder()
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

        final int posX = buildingMiner.getLocation().getX();
        final int posY = buildingMiner.getLocation().getY() + 2;
        final int posZ = buildingMiner.getLocation().getZ();
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
        buildingMiner.setShaftStart(new BlockPos(x, getLastLadder(buildingMiner.getLadderLocation()) - 1, z));
        buildingMiner.setFoundLadder(true);
    }

    private AIState doShaftMining()
    {
        currentWorkingLocation = getNextBlockInShaftToMine();
        if (currentWorkingLocation == null)
        {
            return advanceLadder(MINER_MINING_SHAFT);
        }

        //Note for future me:
        //we have to return; on false of this method
        //but omitted because end of method.
        mineBlock(currentWorkingLocation, currentStandingPosition);

        return MINER_MINING_SHAFT;
    }

    private AIState advanceLadder(final AIState state)
    {
        if (getOwnBuilding().getStartingLevelShaft() >= 4)
        {
            return MINER_BUILDING_SHAFT;
        }

        if (checkOrRequestItems(new ItemStack(Blocks.COBBLESTONE, 2), new ItemStack(Blocks.LADDER)))
        {
            return state;
        }

        @NotNull final BlockPos safeCobble =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 2, getOwnBuilding().getLadderLocation().getZ());

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
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()), getOwnBuilding().getLadderLocation().getZ());
        @NotNull final BlockPos nextLadder =
          new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 1, getOwnBuilding().getLadderLocation().getZ());
        @NotNull final BlockPos nextCobble =
          new BlockPos(getOwnBuilding().getCobbleLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 1, getOwnBuilding().getCobbleLocation().getZ());

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
        final int lastLadder = getLastLadder(ladderPos);
        if (currentWorkingLocation == null)
        {
            currentWorkingLocation = new BlockPos(ladderPos.getX(), lastLadder + 1, ladderPos.getZ());
        }
        Block block = getBlock(currentWorkingLocation);
        if (block != null
              && block != Blocks.AIR
              && block != Blocks.LADDER
              && !(block.equals(Blocks.FLOWING_WATER)
                     || block.equals(Blocks.FLOWING_LAVA)))
        {
            return currentWorkingLocation;
        }
        currentStandingPosition = currentWorkingLocation;
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
                final double distance = curBlock.distanceSq(ladderPos) + Math.pow(curBlock.distanceSq(currentWorkingLocation), 2);
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

    private boolean buildNextBlockInShaft()
    {
        final BlockPos ladderPos = getOwnBuilding().getLadderLocation();
        final int lastLadder = getLastLadder(ladderPos) + 1;

        final int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        final int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();
        //TODO: Really ugly building code, change to structures

        //make area around it safe
        for (int x = -SAFE_CHECK_RANGE + xOffset; x <= SAFE_CHECK_RANGE + xOffset; x++)
        {
            for (int z = -SAFE_CHECK_RANGE + zOffset; z <= SAFE_CHECK_RANGE + zOffset; z++)
            {
                for (int y = SAFE_CHECK_UPPER_BOUND; y >= SAFE_CHECK_LOWER_BOUND; y--)
                {
                    if ((x == 0 && 0 == z) || lastLadder + y <= 1)
                    {
                        continue;
                    }
                    @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder + y, ladderPos.getZ() + z);
                    final int normalizedX = x - xOffset;
                    final int normalizedZ = z - zOffset;
                    if ((Math.abs(normalizedX) > SHAFT_RADIUS
                           || Math.abs(normalizedZ) > SHAFT_RADIUS)
                          && !notReplacedInSecuringMine.contains(world.getBlockState(curBlock).getBlock()))
                    {
                        if (!mineBlock(curBlock, getOwnBuilding().getLocation()))
                        {
                            //make securing go fast as to not confuse the player
                            setDelay(1);
                            return true;
                        }
                        if (checkOrRequestItems(new ItemStack(Blocks.COBBLESTONE)))
                        {
                            return true;
                        }
                        setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
                        return true;
                    }
                }
            }
        }

        //Build the planks
        for (int x = -SHAFT_RADIUS + xOffset; x <= SHAFT_RADIUS + xOffset; x++)
        {
            for (int z = -SHAFT_RADIUS + zOffset; z <= SHAFT_RADIUS + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                final int normalizedX = x - xOffset;
                final int normalizedZ = z - zOffset;
                if ((Math.abs(normalizedX) >= 2 || Math.abs(normalizedZ) >= 2) && world.getBlockState(curBlock) != getOwnBuilding().getFloorBlock())
                {
                    setDelay(DELAY_TIMEOUT);
                    if (checkOrRequestItems(new ItemStack(getOwnBuilding().getFloorBlock().getBlock())))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, getOwnBuilding().getFloorBlock().getBlock(), getOwnBuilding().getFloorBlock());
                    return true;
                }
            }
        }
        //Build fence
        for (int x = -SHAFT_RADIUS + xOffset; x <= SHAFT_RADIUS + xOffset; x++)
        {
            for (int z = -SHAFT_RADIUS + zOffset; z <= SHAFT_RADIUS + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder + 1, ladderPos.getZ() + z);
                final int normalizedX = x - xOffset;
                final int normalizedZ = z - zOffset;
                if (((Math.abs(normalizedX) == 2
                        && Math.abs(normalizedZ) < SHAFT_RADIUS)
                       || (Math.abs(normalizedZ) == 2
                             && Math.abs(normalizedX) < SHAFT_RADIUS))
                      && world.getBlockState(curBlock).getBlock() != getOwnBuilding().getFenceBlock())
                {
                    setDelay(DELAY_TIMEOUT);
                    if (checkOrRequestItems(new ItemStack(getOwnBuilding().getFenceBlock())))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, getOwnBuilding().getFenceBlock());
                    return true;
                }
            }
        }
        //Build torches
        for (int x = -SHAFT_RADIUS + xOffset; x <= SHAFT_RADIUS + xOffset; x++)
        {
            for (int z = -SHAFT_RADIUS + zOffset; z <= SHAFT_RADIUS + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                @NotNull final BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder + 2, ladderPos.getZ() + z);
                final int normalizedX = x - xOffset;
                final int normalizedZ = z - zOffset;
                if (Math.abs(normalizedX) == 2 && Math.abs(normalizedZ) == 2 && world.getBlockState(curBlock).getBlock() != Blocks.TORCH)
                {
                    setDelay(DELAY_TIMEOUT);
                    if (checkOrRequestItems(new ItemStack(Blocks.TORCH)))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, Blocks.TORCH);
                    return true;
                }
            }
        }

        @NotNull final Level currentLevel = new Level(getOwnBuilding(), lastLadder);
        getOwnBuilding().addLevel(currentLevel);
        getOwnBuilding().setCurrentLevel(getOwnBuilding().getNumberOfLevels());
        //Send out update to client
        getOwnBuilding().markDirty();
        return false;
    }

    @NotNull
    private AIState doShaftBuilding()
    {
        if (walkToBuilding())
        {
            return MINER_BUILDING_SHAFT;
        }
        if (buildNextBlockInShaft())
        {
            return MINER_BUILDING_SHAFT;
        }
        getOwnBuilding().resetStartingLevelShaft();

        return START_WORKING;
    }

    @NotNull
    private AIState doNodeMining()
    {
        @Nullable final Level currentLevel = getOwnBuilding().getCurrentLevel();
        if (currentLevel == null)
        {
            Log.getLogger().warn("Current Level not set, resetting...");
            getOwnBuilding().setCurrentLevel(getOwnBuilding().getNumberOfLevels() - 1);
            return doNodeMining();
        }
        mineAtLevel(currentLevel);
        return MINER_CHECK_MINESHAFT;
    }

    private void mineAtLevel(@NotNull final Level currentLevel)
    {
        if (workingNode == null)
        {
            workingNode = findNodeOnLevel(currentLevel);
            return;
        }
        //Looking for a node to stand on while mining workingNode
        int foundDirection = 0;
        @Nullable Node foundNode = null;
        @NotNull final List<Integer> directions = Arrays.asList(1, 2, 3, 4);

        for (final Integer dir : directions)
        {
            final Optional<Node> node = tryFindNodeInDirectionOfNode(currentLevel, workingNode, dir);
            if (node.isPresent() && getNodeStatusForDirection(node.get(), invertDirection(dir)) == Node.NodeStatus.COMPLETED)
            {
                foundDirection = dir;
                foundNode = node.get();
                break;
            }
        }
        if (foundNode == null || foundDirection <= 0)
        {
            workingNode = null;
            return;
        }
        int xOffSet = getXDistance(foundDirection) / 2;
        int zOffSet = getZDistance(foundDirection) / 2;
        if (xOffSet > 0)
        {
            xOffSet += 1;
        }
        else
        {
            xOffSet -= 1;
        }
        if (zOffSet > 0)
        {
            zOffSet += 1;
        }
        else
        {
            zOffSet -= 1;
        }
        @NotNull final BlockPos standingPosition = new BlockPos(workingNode.getX() + xOffSet, currentLevel.getDepth(), workingNode.getZ() + zOffSet);
        currentStandingPosition = standingPosition;
        if (workingNode.getStatus() == Node.NodeStatus.IN_PROGRESS || workingNode.getStatus() == Node.NodeStatus.COMPLETED || !walkToBlock(standingPosition))
        {
            mineNodeFromStand(workingNode, standingPosition, foundDirection);
        }
    }

    private static boolean isOre(final Block block)
    {
        //TODO make this more sophisticated
        return block instanceof BlockOre;
    }

    private boolean secureBlock(@NotNull final BlockPos curBlock, @NotNull final BlockPos safeStand)
    {
        if ((!getBlockState(curBlock).getMaterial().blocksMovement() && getBlock(curBlock) != Blocks.TORCH) || isOre(getBlock(curBlock)))
        {

            if (!mineBlock(curBlock, safeStand))
            {
                //make securing go fast to not confuse the player
                setDelay(1);
                return false;
            }
            if (checkOrRequestItems(new ItemStack(Blocks.COBBLESTONE)))
            {
                return false;
            }

            setBlockFromInventory(curBlock, Blocks.COBBLESTONE);
            //To set it to clean stone... would be cheating
            return false;
        }
        return true;
    }

    private void mineNodeFromStand(@NotNull final Node mineNode, @NotNull final BlockPos standingPosition, final int direction)
    {
        //Preload structures
        if (job.getStructure() == null)
        {
            if (mineNode.getStyle() == Node.NodeType.CROSSROAD)
            {
                loadStructure("miner/minerX4");
            }
            if (mineNode.getStyle() == Node.NodeType.BEND)
            {
                loadStructure("miner/minerX2Right");
            }
            if (mineNode.getStyle() == Node.NodeType.TUNNEL)
            {
                loadStructure("miner/minerX2Top");
            }

            if (job.getStructure() != null)
            {
                job.getStructure().setPosition(new BlockPos(mineNode.getX(), getOwnBuilding().getCurrentLevel().getDepth() + 1, mineNode.getZ()));

                int rotateTimes = 2;
                if (direction == 3)
                {
                    rotateTimes = 3;
                }
                else if (direction == 2)
                {
                    rotateTimes = 0;
                }
                else if (direction == 4)
                {
                    rotateTimes = 1;
                }

                job.getStructure().rotate(rotateTimes);
            }
        }


        //Check for liquids
        for (int x = -NODE_DISTANCE / 2 - 1; x <= NODE_DISTANCE / 2 + 1; x++)
        {
            for (int z = -NODE_DISTANCE / 2 - 1; z <= NODE_DISTANCE / 2 + 1; z++)
            {
                for (int y = -1; y <= 5; y++)
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

        //Check for safe Node
        for (int x = -NODE_DISTANCE / 2; x <= NODE_DISTANCE / 2; x++)
        {
            for (int z = -NODE_DISTANCE / 2; z <= NODE_DISTANCE / 2; z++)
            {
                for (int y = 0; y <= 4; y++)
                {
                    @NotNull final BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    if ((((Math.abs(x) >= 2) && (Math.abs(z) >= 2)) || (getBlock(curBlock) != Blocks.AIR) || (y < 1) || (y > 3)) && !secureBlock(curBlock, standingPosition))
                    {
                        return;
                    }
                }
            }
        }

        if (!mineSideOfNode(mineNode, direction, standingPosition))
        {
            return;
        }

        if (mineNode.getStatus() == Node.NodeStatus.AVAILABLE)
        {
            mineNode.setStatus(Node.NodeStatus.IN_PROGRESS);
        }

        int xOffSet = getXDistance(direction) / 2;
        int zOffSet = getZDistance(direction) / 2;
        if (xOffSet > 0)
        {
            xOffSet -= 1;
        }
        else
        {
            xOffSet += 1;
        }
        if (zOffSet > 0)
        {
            zOffSet -= 1;
        }
        else
        {
            zOffSet += 1;
        }
        @NotNull final BlockPos newStandingPosition = new BlockPos(mineNode.getX() + xOffSet, standingPosition.getY(), mineNode.getZ() + zOffSet);
        currentStandingPosition = newStandingPosition;

        if (mineNode.getStatus() != Node.NodeStatus.COMPLETED)
        {
            //Mine middle
            for (int y = 1; y <= 3; y++)
            {
                for (int x = -1; x <= 1; x++)
                {
                    for (int z = -1; z <= 1; z++)
                    {
                        @NotNull final BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                        if (getBlock(curBlock) == Blocks.TORCH || getBlock(curBlock) == getOwnBuilding().getShaftBlock() || getBlock(curBlock) == getOwnBuilding().getFenceBlock())
                        {
                            continue;
                        }
                        if (!mineBlock(curBlock, newStandingPosition))
                        {
                            return;
                        }
                    }
                }
            }
        }

        @NotNull final List<Integer> directions = Arrays.asList(1, 2, 3, 4);
        for (final Integer dir : directions)
        {
            @NotNull final BlockPos sideStandingPosition = new BlockPos(mineNode.getX() + getXDistance(dir) / 3, standingPosition.getY(), mineNode.getZ() + getZDistance(dir) / 3);
            currentStandingPosition = sideStandingPosition;
            if (!mineSideOfNode(mineNode, dir, sideStandingPosition))
            {
                return;
            }
        }

        //Build middle
        //TODO: make it look nicer!
        if (!buildNodeSupportStructure(mineNode))
        {
            return;
        }

        if (mineNode.getStatus() == Node.NodeStatus.IN_PROGRESS)
        {
            mineNode.setStatus(Node.NodeStatus.COMPLETED);
        }

        workingNode = null;
    }

    private boolean buildNodeSupportStructure(@NotNull final Node mineNode)
    {
        if (mineNode.getStyle() == Node.NodeType.CROSSROAD || mineNode.getStyle() == Node.NodeType.BEND || mineNode.getStyle() == Node.NodeType.TUNNEL)
        {
            return executeStructurePlacement();
        }
        if (mineNode.getStyle() == Node.NodeType.LADDER_BACK)
        {
            //already done
            return true;
        }
        Log.getLogger().info("None of the above: " + mineNode);
        return false;
    }

    private boolean requestBlock()
    {
        while (job.getStructure().findNextBlock())
        {
            @Nullable final Block block = job.getStructure().getBlock();

            if (job.getStructure().doesStructureBlockEqualWorldBlock() || block == Blocks.STONE || block == Blocks.AIR)
            {
                continue;
            }

            if (checkOrRequestItems(new ItemStack(block)))
            {
                job.getStructure().reset();
                return false;
            }
        }
        job.getStructure().reset();
        requestedBlock = true;
        buildStructure = false;
        return true;
    }

    private void loadStructure(@NotNull final String name)
    {
        requestedBlock = false;
        buildStructure = false;
        try
        {
            job.setStructure(new StructureWrapper(world, name));
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn(String.format("StructureProxy: (%s) does not exist - removing build request", name), e);
            job.setStructure(null);
        }
    }

    private boolean executeStructurePlacement()
    {
        if (!requestedBlock && !requestBlock())
        {
            return false;
        }

        if (!buildStructure && !buildStructure())
        {
            return false;
        }

        return buildDecoration();
    }

    private boolean buildDecoration()
    {
        if (job.getStructure().getBlock() == null
              || job.getStructure().doesStructureBlockEqualWorldBlock()
              || (job.getStructure().getBlock() != null && job.getStructure()
                                                             .getBlockState()
                                                             .getMaterial()
                                                             .isSolid())
              || job.getStructure().getBlock() == Blocks.AIR)
        {
            return !findNextBlockNonSolid();
        }

        if (!worker.isWorkerAtSiteWithMove(job.getStructure().getPosition(), 3))
        {
            return false;
        }

        @Nullable final Block block = job.getStructure().getBlock();
        @Nullable final IBlockState metadata = job.getStructure().getBlockState();

        final BlockPos coordinates = job.getStructure().getBlockPosition();
        final int x = coordinates.getX();
        final int y = coordinates.getY();
        final int z = coordinates.getZ();

        final Block worldBlock = world.getBlockState(coordinates).getBlock();

        //should never happen
        if (block == null)
        {
            @NotNull final BlockPos local = job.getStructure().getLocalPosition();
            Log.getLogger().error(String.format("StructureProxy has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockNonSolid();
            return false;
        }
        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.BEDROCK
                || block instanceof AbstractBlockHut)
        {
            findNextBlockNonSolid();
            return false;
        }
        final Item item = Item.getItemFromBlock(block);
        worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item == null ? null : new ItemStack(item, 1));

        setBlockFromInventory(new BlockPos(x, y, z), block, metadata);

        if (findNextBlockNonSolid())
        {
            worker.swingArm(worker.getActiveHand());
            return false;
        }

        job.setStructure(null);
        return true;
    }

    private boolean buildStructure()
    {
        if (job.getStructure().getBlock() == null || job.getStructure().doesStructureBlockEqualWorldBlock() || (!job.getStructure().getBlockState().getMaterial().isSolid()
                                                                                                                  && job.getStructure().getBlock() != Blocks.AIR))
        {
            return !findNextBlockSolid();
        }

        if (!worker.isWorkerAtSiteWithMove(job.getStructure().getPosition(), 3))
        {
            return false;
        }

        @Nullable final Block block = job.getStructure().getBlock();
        @Nullable final IBlockState metadata = job.getStructure().getBlockState();

        final BlockPos coordinates = job.getStructure().getBlockPosition();
        final int x = coordinates.getX();
        final int y = coordinates.getY();
        final int z = coordinates.getZ();

        final Block worldBlock = world.getBlockState(coordinates).getBlock();

        //should never happen
        if (block == null)
        {
            @NotNull final BlockPos local = job.getStructure().getLocalPosition();
            Log.getLogger().error(String.format("StructureProxy has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockSolid();
            return false;
        }

        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.BEDROCK
                || block instanceof AbstractBlockHut || job.getStructure().getBlock() == Blocks.STONE)
        {
            findNextBlockSolid();
            return false;
        }

        if (block != Blocks.AIR)
        {
            final Item item = Item.getItemFromBlock(block);
            worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item == null ? null : new ItemStack(item, 1));
            setBlockFromInventory(new BlockPos(x, y, z), block, metadata);
        }


        if (findNextBlockSolid())
        {
            worker.swingArm(worker.getActiveHand());
            return false;
        }
        job.getStructure().reset();
        buildStructure = true;
        return true;
    }

    private boolean findNextBlockNonSolid()
    {
        //method returns false if there is no next block (structures finished)
        if (!job.getStructure().findNextBlockNonSolid())
        {
            job.getStructure().incrementBlock();
            job.getStructure().reset();
            job.setStructure(null);
            return false;
        }
        return true;
    }

    private boolean findNextBlockSolid()
    {
        //method returns false if there is no next block (structures finished)
        if (!job.getStructure().findNextBlockSolid())
        {
            job.getStructure().incrementBlock();
            job.getStructure().reset();
            buildStructure = true;
            return false;
        }
        return true;
    }

    private boolean mineSideOfNode(@NotNull final Node mineNode, final int direction, @NotNull final BlockPos standingPosition)
    {
        if (getNodeStatusForDirection(mineNode, direction) == Node.NodeStatus.LADDER)
        {
            return true;
        }

        if (getNodeStatusForDirection(mineNode, direction) == Node.NodeStatus.AVAILABLE)
        {
            setNodeStatusForDirection(mineNode, direction, Node.NodeStatus.IN_PROGRESS);
        }

        final int xoffset = getXDistance(direction) / 2;
        final int zoffset = getZDistance(direction) / 2;
        int posx = 1;
        int negx = -1;
        int posz = 1;
        int negz = -1;
        if (xoffset > 0)
        {
            posx = xoffset;
            negx = 2;
        }
        if (xoffset < 0)
        {
            negx = xoffset;
            posx = -2;
        }
        if (zoffset > 0)
        {
            posz = zoffset;
            negz = 2;
        }
        if (zoffset < 0)
        {
            negz = zoffset;
            posz = -2;
        }

        //Mine side
        //TODO: make it look nicer!
        for (int y = 1; y <= 3; y++)
        {
            for (int x = negx; x <= posx; x++)
            {
                for (int z = negz; z <= posz; z++)
                {
                    @NotNull final BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    if (getBlock(curBlock) == Blocks.TORCH || getBlock(curBlock) == getOwnBuilding().getShaftBlock() || getBlock(curBlock) == getOwnBuilding().getFenceBlock())
                    {
                        continue;
                    }
                    if (getNodeStatusForDirection(mineNode, direction) == Node.NodeStatus.WALL)
                    {
                        secureBlock(curBlock, standingPosition);
                    }
                    else if (!mineBlock(curBlock, standingPosition))
                    {
                        return false;
                    }
                }
            }
        }
        if (getNodeStatusForDirection(mineNode, direction) == Node.NodeStatus.IN_PROGRESS)
        {
            setNodeStatusForDirection(mineNode, direction, Node.NodeStatus.COMPLETED);
        }
        return true;
    }

    private static void setNodeStatusForDirection(@NotNull final Node node, final int direction, final Node.NodeStatus status)
    {
        if (direction == 1)
        {
            node.setDirectionPosX(status);
        }
        else if (direction == 2)
        {
            node.setDirectionNegX(status);
        }
        else if (direction == 3)
        {
            node.setDirectionPosZ(status);
        }
        else if (direction == 4)
        {
            node.setDirectionNegZ(status);
        }
    }

    private static Node.NodeStatus getNodeStatusForDirection(@NotNull final Node node, final int direction)
    {
        if (direction == 1)
        {
            return node.getDirectionPosX();
        }
        else if (direction == 2)
        {
            return node.getDirectionNegX();
        }
        else if (direction == 3)
        {
            return node.getDirectionPosZ();
        }
        else if (direction == 4)
        {
            return node.getDirectionNegZ();
        }
        //Cannot happen, so send something that blocks mining
        return Node.NodeStatus.LADDER;
    }

    private static int invertDirection(final int direction)
    {
        if (direction == 1)
        {
            return 2;
        }
        else if (direction == 2)
        {
            return 1;
        }
        else if (direction == 3)
        {
            return 4;
        }
        else if (direction == 4)
        {
            return 3;
        }
        return 0;
    }

    private boolean isNodeInDirectionOfOtherNode(@NotNull final Node start, final int direction, @NotNull final Node check)
    {
        return start.getX() + getXDistance(direction) == check.getX() && start.getZ() + getZDistance(direction) == check.getZ();
    }

    private static int getXDistance(final int direction)
    {
        if (direction == 1)
        {
            return NODE_DISTANCE;
        }
        else if (direction == 2)
        {
            return -NODE_DISTANCE;
        }
        return 0;
    }

    private static int getZDistance(final int direction)
    {
        if (direction == 3)
        {
            return NODE_DISTANCE;
        }
        else if (direction == 4)
        {
            return -NODE_DISTANCE;
        }
        return 0;
    }

    private Optional<Node> tryFindNodeInDirectionOfNode(@NotNull final Level curlevel, final Node start, final int direction)
    {
        final Node finalCurrentNode = start;
        return curlevel.getNodes()
                 .parallelStream()
                 .filter(check -> isNodeInDirectionOfOtherNode(finalCurrentNode, direction, check))
                 .findFirst();
    }

    @NotNull
    private Node createNewNodeInDirectionFromNode(@NotNull final Node start, final int direction)
    {
        final int x = start.getX() + getXDistance(direction);
        final int z = start.getZ() + getZDistance(direction);
        @NotNull final Node node = new Node(x, z);
        node.setStyle(getRandomNodeType());
        if (node.getStyle() == Node.NodeType.TUNNEL)
        {
            final int otherDirection = Math.max(direction, invertDirection(direction)) == 2 ? 4 : 2;
            setNodeStatusForDirection(node, otherDirection, Node.NodeStatus.WALL);
            setNodeStatusForDirection(node, invertDirection(otherDirection), Node.NodeStatus.WALL);
        }
        if (node.getStyle() == Node.NodeType.BEND)
        {
            setNodeStatusForDirection(node, direction, Node.NodeStatus.WALL);
            int otherDirection = Math.max(direction, invertDirection(direction)) == 2 ? 4 : 2;
            //Make Bend go to random side
            if (Math.random() > 0.5)
            {
                otherDirection = invertDirection(otherDirection);
            }
            setNodeStatusForDirection(node, otherDirection, Node.NodeStatus.WALL);
        }
        //No need to do anything for CROSSROAD
        return node;
    }

    @NotNull
    private static Node.NodeType getRandomNodeType()
    {
        final int roll = new Random().nextInt(100);
        if (roll > 50)
        {
            return Node.NodeType.TUNNEL;
        }
        if (roll > 20)
        {
            return Node.NodeType.BEND;
        }
        return Node.NodeType.CROSSROAD;
    }

    private Node findNodeOnLevel(@NotNull final Level currentLevel)
    {
        @Nullable Node currentNode = currentLevel.getLadderNode();
        @NotNull final LinkedList<Node> visited = new LinkedList<>();
        while (currentNode != null)
        {
            if (visited.contains(currentNode))
            {
                return null;
            }

            visited.add(currentNode);
            if (currentNode.getStatus() == Node.NodeStatus.AVAILABLE || currentNode.getStatus() == Node.NodeStatus.IN_PROGRESS)
            {
                return currentNode;
            }

            @NotNull final List<Integer> directions = Arrays.asList(1, 2, 3, 4);
            Collections.shuffle(directions);
            for (final Integer dir : directions)
            {
                final Node.NodeStatus status = getNodeStatusForDirection(currentNode, dir);
                if (status == Node.NodeStatus.AVAILABLE || status == Node.NodeStatus.IN_PROGRESS)
                {
                    return currentNode;
                }
                if (status == Node.NodeStatus.COMPLETED)
                {
                    final Optional<Node> first = tryFindNodeInDirectionOfNode(currentLevel, currentNode, dir);
                    if (first.isPresent())
                    {
                        if (visited.contains(first.get()))
                        {
                            //Stop endless loops
                            continue;
                        }
                        if (getNodeStatusForDirection(first.get(), invertDirection(dir)) == Node.NodeStatus.WALL)
                        {
                            //We got to a wall, not useful
                            continue;
                        }
                        currentNode = first.get();
                        //Out of direction for loop
                        break;
                    }

                    @NotNull final Node newnode = createNewNodeInDirectionFromNode(currentNode, dir);
                    currentLevel.addNode(newnode);
                    return newnode;
                }
            }
        }

        return null;
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, @NotNull final Block block)
    {
        worker.swingArm(worker.getActiveHand());
        setBlockFromInventory(location, block, block.getDefaultState());
    }

    private void setBlockFromInventory(@NotNull final BlockPos location, final Block block, final IBlockState metadata)
    {
        final int slot = worker.findFirstSlotInInventoryWith(block);
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
            world.setBlockState(location, metadata, 3);
        }
    }

    private Block getBlock(@NotNull final BlockPos loc)
    {
        return world.getBlockState(loc).getBlock();
    }

    private int getLastLadder(@NotNull final BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, null))
        {
            return getLastLadder(pos.down());
        }
        else
        {
            return pos.getY() + 1;
        }
    }

    private int getFirstLadder(@NotNull final BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world.getBlockState(pos), world, pos, null))
        {
            return getFirstLadder(pos.up());
        }
        else
        {
            return pos.getY() - 1;
        }
    }
}
