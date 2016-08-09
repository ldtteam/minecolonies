package com.minecolonies.entity.ai.citizen.miner;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.util.Log;
import com.minecolonies.util.SchematicWrapper;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Raycoms, Kostronor
 */

//Todo liquid handling + dump inventory
public class EntityAIStructureMiner extends AbstractEntityAIStructure<JobMiner>
{

    private static final String RENDER_META_TORCH = "Torch";
    private static final int        NODE_DISTANCE             = 7;
    /**
     * Return to chest after 5 stacks
     */
    private static final int        MAX_BLOCKS_MINED          = 64 * 5;
    /*
    Blocks that will be ignored while building shaft/node walls and are certainly safe.
     */
    private static final Set<Block> notReplacedInSecuringMine = new HashSet<>(Arrays.asList(Blocks.cobblestone, Blocks.stone, Blocks.dirt));
    public static final  int DELAY_TIMEOUT = 10;
    public static final int LADDER_SEARCH_RANGE = 10;
    public static final int SHAFT_RADIUS = 3;
    public static final int SAFE_CHECK_RANGE = 5;
    public static final int SAFE_CHECK_UPPER_BOUND = 4;
    public static final int SAFE_CHECK_LOWER_BOUND = -7;
    //The current block to mine
    private BlockPos currentWorkingLocation;
    //the last safe location now being air
    private BlockPos currentStandingPosition;
    private Node    workingNode    = null;
    private boolean requestedBlock = false;
    private boolean buildStructure = false;

    /**
     * Constructor for the Miner.
     * Defines the tasks the miner executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIStructureMiner(JobMiner job)
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
    }

    //Miner wants to work but is not at building
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return START_WORKING;
        }
        //Miner is at building
        return PREPARING;
    }

    private AIState prepareForMining()
    {
        if (!getOwnBuilding().hasFoundLadder())
        {
            return MINER_SEARCHING_LADDER;
        }
        return MINER_CHECK_MINESHAFT;
    }

    @Override
    protected BuildingMiner getOwnBuilding()
    {
        return (BuildingMiner) worker.getWorkBuilding();
    }

    /**
     * Walking to the ladder to check out the mine
     *
     * @return next AIState
     */
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

    @Override
    protected void updateRenderMetaData()
    {
        String renderMetaData = getRenderMetaTorch();
        //TODO: Have pickaxe etc. displayed?
        worker.setRenderMetadata(renderMetaData);
    }

    private String getRenderMetaTorch()
    {
        if (worker.hasItemInInventory(Blocks.torch))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    @Override
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }

    /**
     * Can be overridden by implementations to specify items useful for the worker.
     * When the workers inventory is full, he will try to keep these items.
     * ItemStack amounts are ignored, the first stack found will be taken.
     *
     * @return a list with items nice to have for the worker
     */
    @NotNull
    @Override
    protected List<ItemStack> itemsNiceToHave()
    {
        return Arrays.asList(new ItemStack(Blocks.ladder),
                             new ItemStack(Blocks.planks),
                             new ItemStack(Blocks.oak_fence),
                             new ItemStack(Blocks.torch),
                             new ItemStack(Blocks.cobblestone));
    }

    private AIState lookForLadder()
    {
        BuildingMiner buildingMiner = getOwnBuilding();

        //Check for already found ladder
        if (buildingMiner.hasFoundLadder() && buildingMiner.getLadderLocation() != null)
        {
            if (world.getBlockState(buildingMiner.getLadderLocation()).getBlock() == Blocks.ladder)
            {
                return MINER_WALKING_TO_LADDER;
            }
            else
            {
                buildingMiner.setFoundLadder(false);
                buildingMiner.setLadderLocation(null);
            }
        }

        int posX = buildingMiner.getLocation().getX();
        int posY = buildingMiner.getLocation().getY() + 2;
        int posZ = buildingMiner.getLocation().getZ();
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

    private void tryFindLadderAt(BlockPos pos)
    {
        BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.hasFoundLadder())
        {
            return;
        }
        if (world.getBlockState(pos).getBlock().equals(Blocks.ladder))
        {
            int firstLadderY = getFirstLadder(pos);
            buildingMiner.setLadderLocation(new BlockPos(pos.getX(), firstLadderY, pos.getZ()));
            validateLadderOrientation();
        }
    }

    private void validateLadderOrientation()
    {
        BuildingMiner buildingMiner = getOwnBuilding();

        //TODO: for 1.8 change to getBlockState
        EnumFacing ladderOrientation = world.getBlockState(buildingMiner.getLadderLocation()).getValue(BlockLadder.FACING);
        //http://minecraft.gamepedia.com/Ladder

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

        int x = buildingMiner.getLadderLocation().getX();
        int y = buildingMiner.getLadderLocation().getY();
        int z = buildingMiner.getLadderLocation().getZ();

        buildingMiner.setCobbleLocation(new BlockPos(x - buildingMiner.getVectorX(), y, z - buildingMiner.getVectorZ()));
        buildingMiner.setShaftStart(new BlockPos(x, getLastLadder(new BlockPos(x, y, z)) - 1, z));
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


    private AIState advanceLadder(AIState state)
    {
        if (getOwnBuilding().getStartingLevelShaft() >= 4)
        {
            return MINER_BUILDING_SHAFT;
        }

        if (checkOrRequestItems(new ItemStack(Blocks.cobblestone, 2), new ItemStack(Blocks.ladder)))
        {
            return state;
        }

        BlockPos safeCobble =
                new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 2, getOwnBuilding().getLadderLocation().getZ());

        int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();
        //Check for safe floor
        for (int x = -SAFE_CHECK_RANGE + xOffset; x <= SAFE_CHECK_RANGE + xOffset; x++)
        {
            for (int z = -SAFE_CHECK_RANGE + zOffset; z <= SAFE_CHECK_RANGE + zOffset; z++)
            {
                BlockPos curBlock = new BlockPos(safeCobble.getX() + x, safeCobble.getY(), safeCobble.getZ() + z);
                if (!secureBlock(curBlock, currentStandingPosition))
                {
                    return state;
                }
            }
        }

        BlockPos safeStand =
                new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()), getOwnBuilding().getLadderLocation().getZ());
        BlockPos nextLadder =
                new BlockPos(getOwnBuilding().getLadderLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 1, getOwnBuilding().getLadderLocation().getZ());
        BlockPos nextCobble =
                new BlockPos(getOwnBuilding().getCobbleLocation().getX(), getLastLadder(getOwnBuilding().getLadderLocation()) - 1, getOwnBuilding().getCobbleLocation().getZ());

        if (!mineBlock(nextCobble, safeStand) || !mineBlock(nextLadder, safeStand))
        {
            //waiting until blocks are mined
            return state;
        }


        //Get ladder orientation
        IBlockState metadata = getBlockState(safeStand);

        //set cobblestone
        setBlockFromInventory(nextCobble, Blocks.cobblestone);
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.ladder, metadata);
        getOwnBuilding().increamentStartingLevelShaft();
        return MINER_CHECK_MINESHAFT;
    }

    private IBlockState getBlockState(BlockPos pos)
    {
        return world.getBlockState(pos);
    }


    /**
     * Calculates the next non-air block to mine.
     * Will take the nearest block it finds.
     */
    private BlockPos getNextBlockInShaftToMine()
    {

        BlockPos ladderPos  = getOwnBuilding().getLadderLocation();
        int      lastLadder = getLastLadder(ladderPos);
        if (currentWorkingLocation == null)
        {
            currentWorkingLocation = new BlockPos(ladderPos.getX(), lastLadder + 1, ladderPos.getZ());
        }
        Block block = getBlock(currentWorkingLocation);
        if (block != null && block != Blocks.air && block != Blocks.ladder)
        {
            return currentWorkingLocation;
        }
        currentStandingPosition = currentWorkingLocation;
        BlockPos nextBlockToMine = null;
        double   bestDistance    = Double.MAX_VALUE;

        int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();

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
                BlockPos curBlock = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                double   distance = curBlock.distanceSq(ladderPos) + Math.pow(curBlock.distanceSq(currentWorkingLocation), 2);
                if (distance < bestDistance && !world.isAirBlock(curBlock))
                {
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
                    BlockPos curBlock = new BlockPos(nextBlockToMine.getX() + x, lastLadder, nextBlockToMine.getZ() + z);
                    double   distance = curBlock.distanceSq(ladderPos);
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
        BlockPos ladderPos  = getOwnBuilding().getLadderLocation();
        int      lastLadder = getLastLadder(ladderPos) + 1;

        int xOffset = SHAFT_RADIUS * getOwnBuilding().getVectorX();
        int zOffset = SHAFT_RADIUS * getOwnBuilding().getVectorZ();
        //TODO: Really ugly building code, change to schematics

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
                    BlockPos curBlock    = new BlockPos(ladderPos.getX() + x, lastLadder + y, ladderPos.getZ() + z);
                    int      normalizedX = x - xOffset;
                    int      normalizedZ = z - zOffset;
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
                        if (checkOrRequestItems(new ItemStack(Blocks.cobblestone)))
                        {
                            return true;
                        }
                        setBlockFromInventory(curBlock, Blocks.cobblestone);
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
                BlockPos curBlock    = new BlockPos(ladderPos.getX() + x, lastLadder, ladderPos.getZ() + z);
                int      normalizedX = x - xOffset;
                int      normalizedZ = z - zOffset;
                if ((Math.abs(normalizedX) >= 2 || Math.abs(normalizedZ) >= 2) && world.getBlockState(curBlock).getBlock() != getOwnBuilding().getFloorBlock())
                {
                    setDelay(DELAY_TIMEOUT);
                    if (checkOrRequestItems(new ItemStack(getOwnBuilding().getFloorBlock())))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, getOwnBuilding().getFloorBlock());
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
                BlockPos curBlock    = new BlockPos(ladderPos.getX() + x, lastLadder + 1, ladderPos.getZ() + z);
                int      normalizedX = x - xOffset;
                int      normalizedZ = z - zOffset;
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
                BlockPos curBlock    = new BlockPos(ladderPos.getX() + x, lastLadder + 2, ladderPos.getZ() + z);
                int      normalizedX = x - xOffset;
                int      normalizedZ = z - zOffset;
                if (Math.abs(normalizedX) == 2 && Math.abs(normalizedZ) == 2 && world.getBlockState(curBlock).getBlock() != Blocks.torch)
                {
                    setDelay(DELAY_TIMEOUT);
                    if (checkOrRequestItems(new ItemStack(Blocks.torch)))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, Blocks.torch);
                    return true;
                }
            }
        }

        Level currentLevel = new Level(getOwnBuilding(), lastLadder);
        getOwnBuilding().addLevel(currentLevel);
        getOwnBuilding().setCurrentLevel(getOwnBuilding().getNumberOfLevels());
        //Send out update to client
        getOwnBuilding().markDirty();
        return false;
    }

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

    private AIState doNodeMining()
    {
        Level currentLevel = getOwnBuilding().getCurrentLevel();
        if (currentLevel == null)
        {
            Log.logger.warn("Current Level not set, resetting...");
            getOwnBuilding().setCurrentLevel(getOwnBuilding().getNumberOfLevels() - 1);
            return doNodeMining();
        }
        mineAtLevel(currentLevel);
        return MINER_CHECK_MINESHAFT;
    }

    private void mineAtLevel(Level currentLevel)
    {
        if (workingNode == null)
        {
            workingNode = findNodeOnLevel(currentLevel);
            return;
        }
        //Looking for a node to stand on while mining workingNode
        int           foundDirection = 0;
        Node          foundNode      = null;
        List<Integer> directions     = Arrays.asList(1, 2, 3, 4);

        for (Integer dir : directions)
        {
            Optional<Node> node = tryFindNodeInDirectionOfNode(currentLevel, workingNode, dir);
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
        BlockPos standingPosition = new BlockPos(workingNode.getX() + xOffSet, currentLevel.getDepth(), workingNode.getZ() + zOffSet);
        currentStandingPosition = standingPosition;
        if (workingNode.getStatus() == Node.NodeStatus.IN_PROGRESS || workingNode.getStatus() == Node.NodeStatus.COMPLETED || !walkToBlock(standingPosition))
        {
            mineNodeFromStand(workingNode, standingPosition, foundDirection);
        }
    }

    private boolean isOre(Block block)
    {
        //TODO make this more sophisticated
        return block instanceof BlockOre;
    }

    private boolean secureBlock(BlockPos curBlock, BlockPos safeStand)
    {
        if ((!getBlock(curBlock).getMaterial().blocksMovement() && getBlock(curBlock) != Blocks.torch) || isOre(getBlock(curBlock)))
        {

            if (!mineBlock(curBlock, safeStand))
            {
                //make securing go fast to not confuse the player
                setDelay(1);
                return false;
            }
            if (checkOrRequestItems(new ItemStack(Blocks.cobblestone)))
            {
                return false;
            }

            setBlockFromInventory(curBlock, Blocks.cobblestone);
            //To set it to clean stone... would be cheating
            return false;
        }
        return true;
    }


    private void mineNodeFromStand(Node mineNode, BlockPos standingPosition, int direction)
    {
        //todo decide if bend right or left
        //Preload schematics
        if (job.getSchematic() == null)
        {
            if (mineNode.getStyle() == Node.NodeType.CROSSROAD)
            {
                loadSchematic("classic/minerX4");
            }
            if (mineNode.getStyle() == Node.NodeType.BEND)
            {
                loadSchematic("classic/minerX2Right");
            }
            if (mineNode.getStyle() == Node.NodeType.TUNNEL)
            {
                loadSchematic("classic/minerX2Top");
            }

            if (job.getSchematic() != null)
            {
                job.getSchematic().setPosition(new BlockPos(mineNode.getX(), getOwnBuilding().getCurrentLevel().getDepth() + 1, mineNode.getZ()));

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

                job.getSchematic().rotate(rotateTimes);
            }
        }

        //Check for safe Node
        for (int x = -NODE_DISTANCE / 2; x <= NODE_DISTANCE / 2; x++)
        {
            for (int z = -NODE_DISTANCE / 2; z <= NODE_DISTANCE / 2; z++)
            {
                for (int y = 0; y <= 4; y++)
                {
                    BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    if (((Math.abs(x) >= 2) && (Math.abs(z) >= 2)) || (getBlock(curBlock) != Blocks.air) || (y < 1) || (y > 3))
                    {
                        if (!secureBlock(curBlock, standingPosition))
                        {
                            return;
                        }
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
        BlockPos newStandingPosition = new BlockPos(mineNode.getX() + xOffSet, standingPosition.getY(), mineNode.getZ() + zOffSet);
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
                        BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                        if (getBlock(curBlock) == Blocks.torch || getBlock(curBlock) == getOwnBuilding().getFloorBlock() || getBlock(curBlock) == getOwnBuilding().getFenceBlock())
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

        List<Integer> directions = Arrays.asList(1, 2, 3, 4);
        for (Integer dir : directions)
        {
            BlockPos sideStandingPosition = new BlockPos(mineNode.getX() + getXDistance(dir) / 3, standingPosition.getY(), mineNode.getZ() + getZDistance(dir) / 3);
            currentStandingPosition = sideStandingPosition;
            if (!mineSideOfNode(mineNode, dir, sideStandingPosition))
            {
                return;
            }
        }

        //Build middle
        //TODO: make it look nicer!
        if (!buildNodeSupportStructure(mineNode, standingPosition))
        {
            return;
        }

        if (mineNode.getStatus() == Node.NodeStatus.IN_PROGRESS)
        {
            mineNode.setStatus(Node.NodeStatus.COMPLETED);
        }

        workingNode = null;
    }

    private boolean buildNodeSupportStructure(Node mineNode, BlockPos standingPosition)
    {
        if (mineNode.getStyle() == Node.NodeType.CROSSROAD || mineNode.getStyle() == Node.NodeType.BEND || mineNode.getStyle() == Node.NodeType.TUNNEL)
        {
            return executeSchematicPlacement();
        }
        if (mineNode.getStyle() == Node.NodeType.LADDER_BACK)
        {
            return true; //already done
        }
        Log.logger.info("None of the above: " + mineNode);
        return false;
    }

    private boolean requestBlock()
    {
        while (job.getSchematic().findNextBlock())
        {
            Block block = job.getSchematic().getBlock();

            if (job.getSchematic().doesSchematicBlockEqualWorldBlock() || block == Blocks.stone || block == Blocks.air)
            {
                continue;
            }

            if (checkOrRequestItems(new ItemStack(block)))
            {
                job.getSchematic().reset();
                return false;
            }
        }
        job.getSchematic().reset();
        requestedBlock = true;
        buildStructure = false;
        return true;
    }

    private void loadSchematic(String name)
    {
        requestedBlock = false;
        buildStructure = false;
        try
        {
            job.setSchematic(new SchematicWrapper(world, name));
        }
        catch (IllegalStateException e)
        {
            Log.logger.warn(String.format("Schematic: (%s) does not exist - removing build request", name), e);
            job.setSchematic(null);
        }
    }

    private boolean executeSchematicPlacement()
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
        if (job.getSchematic().getBlock() == null
            || job.getSchematic().doesSchematicBlockEqualWorldBlock()
            || (job.getSchematic().getBlock() != null && job.getSchematic()
                                                            .getBlock()
                                                            .getMaterial()
                                                            .isSolid())
            || job.getSchematic().getBlock() == Blocks.air)
        {
            return !findNextBlockNonSolid();
        }

        if (!worker.isWorkerAtSiteWithMove(job.getSchematic().getPosition(), 3))
        {
            return false;
        }

        Block       block    = job.getSchematic().getBlock();
        IBlockState metadata = job.getSchematic().getBlockState();

        BlockPos coordinates = job.getSchematic().getBlockPosition();
        int      x           = coordinates.getX();
        int      y           = coordinates.getY();
        int      z           = coordinates.getZ();

        Block worldBlock = world.getBlockState(coordinates).getBlock();

        if (block == null)//should never happen
        {
            BlockPos local = job.getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockNonSolid();
            return false;
        }
        if (worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.bedrock ||
            block instanceof AbstractBlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockNonSolid();
            return false;
        }
        Item item = Item.getItemFromBlock(block);
        worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);

        setBlockFromInventory(new BlockPos(x, y, z), block, metadata);

        if (findNextBlockNonSolid())
        {
            worker.swingItem();
            return false;
        }

        job.setSchematic(null);
        return true;
    }

    private boolean buildStructure()
    {
        if (job.getSchematic().getBlock() == null || job.getSchematic().doesSchematicBlockEqualWorldBlock() || (!job.getSchematic().getBlock().getMaterial().isSolid()
                                                                                                                && job.getSchematic().getBlock() != Blocks.air))
        {
            return !findNextBlockSolid();
        }

        if (!worker.isWorkerAtSiteWithMove(job.getSchematic().getPosition(), 3))
        {
            return false;
        }

        Block       block    = job.getSchematic().getBlock();
        IBlockState metadata = job.getSchematic().getBlockState();

        BlockPos coordinates = job.getSchematic().getBlockPosition();
        int      x           = coordinates.getX();
        int      y           = coordinates.getY();
        int      z           = coordinates.getZ();

        Block worldBlock = world.getBlockState(coordinates).getBlock();

        if (block == null)//should never happen
        {
            BlockPos local = job.getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockSolid();
            return false;
        }
        if (worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.bedrock ||
            block instanceof AbstractBlockHut || job.getSchematic().getBlock() == Blocks.stone)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockSolid();
            return false;
        }

        if (!(block == Blocks.air))
        {
            Item item = Item.getItemFromBlock(block);
            worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);
            setBlockFromInventory(new BlockPos(x, y, z), block, metadata);
        }

        if (findNextBlockSolid())
        {
            worker.swingItem();
            return false;
        }
        job.getSchematic().reset();
        buildStructure = true;
        return true;
    }

    private boolean findNextBlockNonSolid()
    {
        if (!job.getSchematic().findNextBlockNonSolid())//method returns false if there is no next block (schematic finished)
        {
            job.getSchematic().incrementBlock();
            job.getSchematic().reset();
            return false;
        }
        return true;
    }

    private boolean findNextBlockSolid()
    {
        if (!job.getSchematic().findNextBlockSolid())//method returns false if there is no next block (schematic finished)
        {
            job.getSchematic().incrementBlock();
            job.getSchematic().reset();
            buildStructure = true;
            return false;
        }
        return true;
    }

    private boolean mineSideOfNode(Node mineNode, int direction, BlockPos standingPosition)
    {
        if (getNodeStatusForDirection(mineNode, direction) == Node.NodeStatus.LADDER)
        {
            return true;
        }

        if (getNodeStatusForDirection(mineNode, direction) == Node.NodeStatus.AVAILABLE)
        {
            setNodeStatusForDirection(mineNode, direction, Node.NodeStatus.IN_PROGRESS);
        }

        int xoffset = getXDistance(direction) / 2;
        int zoffset = getZDistance(direction) / 2;
        int posx    = 1;
        int negx    = -1;
        int posz    = 1;
        int negz    = -1;
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
                    BlockPos curBlock = new BlockPos(mineNode.getX() + x, standingPosition.getY() + y, mineNode.getZ() + z);
                    if (getBlock(curBlock) == Blocks.torch || getBlock(curBlock) == getOwnBuilding().getFloorBlock() || getBlock(curBlock) == getOwnBuilding().getFenceBlock())
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

    private void setNodeStatusForDirection(Node node, int direction, Node.NodeStatus status)
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

    private Node.NodeStatus getNodeStatusForDirection(Node node, int direction)
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

    private int invertDirection(int direction)
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

    private boolean isNodeInDirectionOfOtherNode(Node start, int direction, Node check)
    {
        return start.getX() + getXDistance(direction) == check.getX() && start.getZ() + getZDistance(direction) == check.getZ();
    }

    private int getXDistance(int direction)
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

    private int getZDistance(int direction)
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

    private Optional<Node> tryFindNodeInDirectionOfNode(Level curlevel, Node start, int direction)
    {
        final Node finalCurrentNode = start;
        return curlevel.getNodes()
                       .parallelStream()
                       .filter(check -> isNodeInDirectionOfOtherNode(finalCurrentNode, direction, check))
                       .findFirst();
    }

    private Node createNewNodeInDirectionFromNode(Node start, int direction)
    {
        int  x    = start.getX() + getXDistance(direction);
        int  z    = start.getZ() + getZDistance(direction);
        Node node = new Node(x, z);
        node.setStyle(getRandomNodeType());
        if (node.getStyle() == Node.NodeType.TUNNEL)
        {
            int otherDirection = Math.max(direction, invertDirection(direction)) == 2 ? 4 : 2;
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

    private Node.NodeType getRandomNodeType()
    {
        int roll = new Random().nextInt(100);
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

    private Node findNodeOnLevel(Level currentLevel)
    {
        Node             currentNode = currentLevel.getLadderNode();
        LinkedList<Node> visited     = new LinkedList<>();
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

            List<Integer> directions = Arrays.asList(1, 2, 3, 4);
            Collections.shuffle(directions);
            for (Integer dir : directions)
            {
                Node.NodeStatus status = getNodeStatusForDirection(currentNode, dir);
                if (status == Node.NodeStatus.AVAILABLE || status == Node.NodeStatus.IN_PROGRESS)
                {
                    return currentNode;
                }
                if (status == Node.NodeStatus.COMPLETED)
                {
                    Optional<Node> first = tryFindNodeInDirectionOfNode(currentLevel, currentNode, dir);
                    if (first.isPresent())
                    {
                        if (visited.contains(first.get()))
                        {
                            continue;//Stop endless loops
                        }
                        if (getNodeStatusForDirection(first.get(), invertDirection(dir)) == Node.NodeStatus.WALL)
                        {
                            continue; //We got to a wall, not useful
                        }
                        currentNode = first.get();
                        break; //Out of direction for loop
                    }

                    Node newnode = createNewNodeInDirectionFromNode(currentNode, dir);
                    currentLevel.addNode(newnode);
                    return newnode;
                }
            }
        }

        return null;
    }

    private void setBlockFromInventory(BlockPos location, Block block)
    {
        setBlockFromInventory(location, block, block.getDefaultState());
    }

    private void setBlockFromInventory(BlockPos location, Block block, IBlockState metadata)
    {
        int slot = worker.findFirstSlotInInventoryWith(block);
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
            world.setBlockState(location, metadata, 3);
        }
    }

    private Block getBlock(BlockPos loc)
    {
        return world.getBlockState(loc).getBlock();
    }

    private int getLastLadder(BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world, pos, null))
        {
            return getLastLadder(pos.down());
        }
        else
        {
            return pos.getY() + 1;
        }
    }

    private int getFirstLadder(BlockPos pos)
    {
        if (world.getBlockState(pos).getBlock().isLadder(world, pos, null))
        {
            return getFirstLadder(pos.up());
        }
        else
        {
            return pos.getY() - 1;
        }
    }
}
