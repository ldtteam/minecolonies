package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Raycoms, Kostronor
 */

public class EntityAIWorkMiner extends AbstractEntityAIWork<JobMiner>
{

    private static final String RENDER_META_TORCH = "Torch";
    private static final int NODE_DISTANCE = 7;
    /*
    Blocks that will be ignored while building shaft/node walls and are certainly safe.
     */
    private static final Set<Block> notReplacedInSecuringMine =
            new HashSet<>(Arrays.asList(
                    Blocks.cobblestone,
                    Blocks.stone,
                    Blocks.dirt
                                       ));
    private static Logger logger = LogManager.getLogger("Miner");
    //The current block to mine
    private ChunkCoordinates currentWorkingLocation;
    //the last safe location now being air
    private ChunkCoordinates currentStandingPosition;
    /**
     * If we have waited one delay
     */
    private boolean hasDelayed = false;
    private Node workingNode = null;


    public EntityAIWorkMiner(JobMiner job)
    {
        super(job);
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
        if (worker.hasitemInInventory(Blocks.torch))
        {
            return RENDER_META_TORCH;
        }
        return "";
    }

    @Override
    protected BuildingMiner getOwnBuilding()
    {
        return (BuildingMiner) worker.getWorkBuilding();
    }


    private boolean walkToLadder()
    {
        return walkToBlock(getOwnBuilding().ladderLocation);
    }

    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }

    private void lookForLadder()
    {
        BuildingMiner buildingMiner = getOwnBuilding();

        //Check for already found ladder
        if (buildingMiner.foundLadder && buildingMiner.ladderLocation != null)
        {
            if (world.getBlock(buildingMiner.ladderLocation.posX,
                               buildingMiner.ladderLocation.posY,
                               buildingMiner.ladderLocation.posZ) == Blocks.ladder)
            {
                job.setStage(Stage.LADDER_FOUND);
                return;
            }
            else
            {
                buildingMiner.foundLadder = false;
                buildingMiner.ladderLocation = null;
            }
        }

        int posX = buildingMiner.getLocation().posX;
        int posY = buildingMiner.getLocation().posY + 2;
        int posZ = buildingMiner.getLocation().posZ;
        for (int y = posY - 10; y < posY; y++)
        {
            for (int x = posX - 10; x < posX + 10; x++)
            {
                for (int z = posZ - 10; z < posZ + 10; z++)
                {
                    tryFindLadderAt(x, y, z);
                }
            }
        }
    }

    private void tryFindLadderAt(int x, int y, int z)
    {
        BuildingMiner buildingMiner = getOwnBuilding();
        if (buildingMiner.foundLadder)
        {
            return;
        }
        if (world.getBlock(x, y, z).equals(Blocks.ladder))
        {
            int firstLadderY = getFirstLadder(x, y, z);
            buildingMiner.ladderLocation = new ChunkCoordinates(x, firstLadderY, z);
            validateLadderOrientation();
        }
    }

    private void validateLadderOrientation()
    {
        BuildingMiner buildingMiner = getOwnBuilding();
        int x = buildingMiner.ladderLocation.posX;
        int y = buildingMiner.ladderLocation.posY;
        int z = buildingMiner.ladderLocation.posZ;

        //TODO: for 1.8 change to getBlockState
        int ladderOrientation = world.getBlockMetadata(x, y, z);
        //http://minecraft.gamepedia.com/Ladder

        if (ladderOrientation == 4)
        {
            //West
            buildingMiner.vectorX = 1;
            buildingMiner.vectorZ = 0;
        }
        else if (ladderOrientation == 5)
        {
            //East
            buildingMiner.vectorX = -1;
            buildingMiner.vectorZ = 0;
        }
        else if (ladderOrientation == 3)
        {
            //South
            buildingMiner.vectorZ = 1;
            buildingMiner.vectorX = 0;
        }
        else if (ladderOrientation == 2)
        {
            //North
            buildingMiner.vectorZ = -1;
            buildingMiner.vectorX = 0;
        }
        else
        {
            throw new IllegalStateException("Ladder metadata was " + ladderOrientation);
        }
        buildingMiner.cobbleLocation = new ChunkCoordinates(x - buildingMiner.vectorX, y, z - buildingMiner.vectorZ);
        buildingMiner.shaftStart = new ChunkCoordinates(x, getLastLadder(x, y, z) - 1, z);
        buildingMiner.foundLadder = true;
    }

    private void requestTool(Block curblock)
    {
        if (Objects.equals(curblock.getHarvestTool(0), SHOVEL))
        {
            job.setStage(Stage.PREPARING);
            needsShovel = true;
        }
        if (Objects.equals(curblock.getHarvestTool(0), PICKAXE))
        {
            job.setStage(Stage.PREPARING);
            needsPickaxe = true;
            needsPickaxeLevel = curblock.getHarvestLevel(0);
        }
    }

    private void doShaftMining()
    {

        currentWorkingLocation = getNextBlockInShaftToMine();
        if (currentWorkingLocation == null)
        {
            advanceLadder();
            return;
        }

        //Note for future me:
        //we have to return; on false of this method
        //but omitted because end of method.
        mineBlock(currentWorkingLocation, currentStandingPosition);
    }

    private boolean missesItemsInInventory(ItemStack... items)
    {
        boolean allClear = true;
        for (ItemStack stack : items)
        {
            int countOfItem = worker.getItemCountInInventory(stack.getItem());
            if (countOfItem < stack.stackSize)
            {
                int itemsLeft = stack.stackSize - countOfItem;
                ItemStack requiredStack = new ItemStack(stack.getItem(), itemsLeft);
                itemsCurrentlyNeeded.add(requiredStack);
                allClear = false;
            }
        }
        if (allClear)
        {
            return false;
        }
        itemsNeeded.clear();
        for (ItemStack stack : items)
        {
            itemsNeeded.add(stack);
        }
        job.setStage(Stage.PREPARING);
        return true;
    }


    private void advanceLadder()
    {
        if (getOwnBuilding().startingLevelShaft >= 5)
        {
            job.setStage(Stage.BUILD_SHAFT);
            return;
        }

        if (missesItemsInInventory(new ItemStack(Blocks.cobblestone, 2), new ItemStack(Blocks.ladder)))
        {
            return;
        }

        ChunkCoordinates safeStand = new ChunkCoordinates(getOwnBuilding().ladderLocation.posX,
                                                          getLastLadder(getOwnBuilding().ladderLocation),
                                                          getOwnBuilding().ladderLocation.posZ);
        ChunkCoordinates nextLadder = new ChunkCoordinates(getOwnBuilding().ladderLocation.posX,
                                                           getLastLadder(getOwnBuilding().ladderLocation) - 1,
                                                           getOwnBuilding().ladderLocation.posZ);
        ChunkCoordinates nextCobble = new ChunkCoordinates(getOwnBuilding().cobbleLocation.posX,
                                                           getLastLadder(getOwnBuilding().ladderLocation) - 1,
                                                           getOwnBuilding().cobbleLocation.posZ);
        ChunkCoordinates safeCobble = new ChunkCoordinates(getOwnBuilding().ladderLocation.posX,
                                                           getLastLadder(getOwnBuilding().ladderLocation) - 2,
                                                           getOwnBuilding().ladderLocation.posZ);

        int xOffset = 3 * getOwnBuilding().vectorX;
        int zOffset = 3 * getOwnBuilding().vectorZ;
        //Check for safe floor
        for (int x = -4 + xOffset; x <= 4 + xOffset; x++)
        {
            for (int z = -4 + zOffset; z <= 4 + zOffset; z++)
            {
                ChunkCoordinates curBlock = new ChunkCoordinates(safeCobble.posX + x,
                                                                 safeCobble.posY,
                                                                 safeCobble.posZ + z);
                if (!secureBlock(curBlock, currentStandingPosition))
                {
                    return;
                }
            }
        }


        if (!mineBlock(nextCobble, safeStand) || !mineBlock(nextLadder, safeStand))
        {
            //waiting until blocks are mined
            return;
        }


        //Get ladder orientation
        int metadata = getBlockMetadata(safeStand);
        //set cobblestone
        setBlockFromInventory(nextCobble, Blocks.cobblestone);
        //set ladder
        setBlockFromInventory(nextLadder, Blocks.ladder, metadata);
        getOwnBuilding().startingLevelShaft++;
        job.setStage(Stage.CHECK_MINESHAFT);
    }

    /**
     * Checks for the right tools and waits for an appropriate delay.
     *
     * @param blockToMine the block to mine eventually
     * @param safeStand   a safe stand to mine from (AIR Block!)
     */
    private boolean checkMiningLocation(ChunkCoordinates blockToMine, ChunkCoordinates safeStand)
    {

        Block curBlock = world.getBlock(blockToMine.posX, blockToMine.posY, blockToMine.posZ);

        if (!holdEfficientTool(curBlock))
        {
            //We are missing a tool to harvest this block...
            requestTool(curBlock);
            return true;
        }

        ItemStack tool = worker.getHeldItem();

        if (curBlock.getHarvestLevel(0) < Utils.getMiningLevel(tool, curBlock.getHarvestTool(0)))
        {
            //We have to high of a tool...
            //TODO: request lower tier tools
        }

        if (tool != null && !ForgeHooks.canToolHarvestBlock(curBlock, 0, tool) && curBlock != Blocks.bedrock)
        {
            logger.info("ForgeHook not in sync with EfficientTool for " + curBlock + " and " + tool + "\n"
                        + "Please report to MineColonies with this text to add support!");
        }
        currentWorkingLocation = blockToMine;
        currentStandingPosition = safeStand;
        if (!hasDelayed)
        {
            workOnBlock(currentWorkingLocation, currentStandingPosition,
                        getBlockMiningDelay(curBlock, blockToMine));
            hasDelayed = true;
            return true;
        }
        hasDelayed = false;
        return false;
    }

    private int getFortuneOf(ItemStack tool)
    {
        if (tool == null)
        {
            return 0;
        }
        //calculate fortune enchantment
        int fortune = 0;
        if (tool.isItemEnchanted())
        {
            NBTTagList t = tool.getEnchantmentTagList();

            for (int i = 0; i < t.tagCount(); i++)
            {
                short id = t.getCompoundTagAt(i).getShort("id");
                if (id == 35)
                {
                    fortune = t.getCompoundTagAt(i).getShort("lvl");
                }
            }
        }
        return fortune;
    }

    /**
     * Will simulate mining a block with particles ItemDrop etc.
     * Attention:
     * Because it simulates delay, it has to be called 2 times.
     * So make sure the code path up to this function is reachable a second time.
     * And make sure to immediately exit the update function when this returns false.
     */
    private boolean mineBlock(ChunkCoordinates blockToMine, ChunkCoordinates safeStand)
    {
        Block curBlock = world.getBlock(blockToMine.posX, blockToMine.posY, blockToMine.posZ);
        if (curBlock == null || curBlock == Blocks.air)
        {
            //no need to mine block...
            return true;
        }

        if (checkMiningLocation(blockToMine, safeStand))
        {
            //we have to wait for delay
            return false;
        }

        ItemStack tool = worker.getHeldItem();


        //calculate fortune enchantment
        int fortune = getFortuneOf(tool);

        //Dangerous TODO: validate that
        //Seems like dispatching the event manually is a bad idea? any clues?
        if (tool != null)
        {
            //Reduce durability if not using hand
            tool.getItem().onBlockDestroyed(tool,
                                            world,
                                            curBlock,
                                            blockToMine.posX,
                                            blockToMine.posY,
                                            blockToMine.posZ,
                                            worker);
        }

        //if Tool breaks
        if (tool != null && tool.stackSize < 1)
        {
            worker.setCurrentItemOrArmor(0, null);
            worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
        }

        Utils.blockBreakSoundAndEffect(world,
                                       blockToMine.posX,
                                       blockToMine.posY,
                                       blockToMine.posZ,
                                       curBlock,
                                       world.getBlockMetadata(blockToMine.posX, blockToMine.posY, blockToMine.posZ));
        //Don't drop bedrock but we want to mine bedrock in some cases...
        if (curBlock != Blocks.bedrock)
        {
            List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, blockToMine, fortune);
            for (ItemStack item : items)
            {
                InventoryUtils.setStack(worker.getInventory(), item);
            }
        }

        world.setBlockToAir(blockToMine.posX, blockToMine.posY, blockToMine.posZ);
        return true;
    }

    /**
     * Calculates the next non-air block to mine.
     * Will take the nearest block it finds.
     */
    private ChunkCoordinates getNextBlockInShaftToMine()
    {

        ChunkCoordinates ladderPos = getOwnBuilding().ladderLocation;
        int lastLadder = getLastLadder(ladderPos);
        if (currentWorkingLocation == null)
        {
            currentWorkingLocation = new ChunkCoordinates(ladderPos.posX, lastLadder + 1, ladderPos.posZ);
        }
        Block block = getBlock(currentWorkingLocation);
        if (block != null && block != Blocks.air && block != Blocks.ladder)
        {
            return currentWorkingLocation;
        }
        currentStandingPosition = currentWorkingLocation;
        ChunkCoordinates nextBlockToMine = null;
        double bestDistance = Double.MAX_VALUE;

        int xOffset = 3 * getOwnBuilding().vectorX;
        int zOffset = 3 * getOwnBuilding().vectorZ;

        //7x7 shaft find nearest block
        //Beware from positive to negative! to draw the miner to a wall to go down
        for (int x = 3 + xOffset; x >= -3 + xOffset; x--)
        {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x, lastLadder, ladderPos.posZ + z);
                double
                        distance =
                        curBlock.getDistanceSquaredToChunkCoordinates(ladderPos)
                        + Math.pow(curBlock.getDistanceSquaredToChunkCoordinates(currentWorkingLocation), 2);
                if (distance < bestDistance && !world.isAirBlock(curBlock.posX, curBlock.posY, curBlock.posZ))
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
                    ChunkCoordinates curBlock = new ChunkCoordinates(nextBlockToMine.posX + x,
                                                                     lastLadder,
                                                                     nextBlockToMine.posZ + z);
                    double distance = curBlock.getDistanceSquaredToChunkCoordinates(ladderPos);
                    if (distance < bestDistance && world.isAirBlock(curBlock.posX, curBlock.posY, curBlock.posZ))
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
        ChunkCoordinates ladderPos = getOwnBuilding().ladderLocation;
        int lastLadder = getLastLadder(ladderPos) + 1;

        int xOffset = 3 * getOwnBuilding().vectorX;
        int zOffset = 3 * getOwnBuilding().vectorZ;
        //TODO: Really ugly building code, change to schematics

        //make area around it safe
        for (int x = -5 + xOffset; x <= 5 + xOffset; x++)
        {
            for (int z = -5 + zOffset; z <= 5 + zOffset; z++)
            {
                for (int y = 5; y >= -7; y--)
                {
                    if ((x == 0 && 0 == z) || lastLadder + y <= 1)
                    {
                        continue;
                    }
                    ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                                                                     lastLadder + y,
                                                                     ladderPos.posZ + z);
                    int normalizedX = x - xOffset;
                    int normalizedZ = z - zOffset;
                    if ((Math.abs(normalizedX) > 3 || Math.abs(normalizedZ) > 3)
                        && !notReplacedInSecuringMine.contains(
                            world.getBlock(curBlock.posX,
                                           curBlock.posY,
                                           curBlock.posZ)))
                    {
                        if (!mineBlock(curBlock, getOwnBuilding().getLocation()))
                        {
                            //make securing go fast as to not confuse the player
                            setDelay(1);
                            return true;
                        }
                        if (missesItemsInInventory(new ItemStack(Blocks.cobblestone)))
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
        for (int x = -3 + xOffset; x <= 3 + xOffset; x++)
        {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x, lastLadder, ladderPos.posZ + z);
                int normalizedX = x - xOffset;
                int normalizedZ = z - zOffset;
                if ((Math.abs(normalizedX) >= 2 || Math.abs(normalizedZ) >= 2)
                    && world.getBlock(curBlock.posX,
                                      curBlock.posY,
                                      curBlock.posZ)
                       != Blocks.planks)
                {
                    setDelay(10);
                    if (missesItemsInInventory(new ItemStack(Blocks.planks)))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, Blocks.planks);
                    return true;
                }
            }
        }
        //Build fence
        for (int x = -3 + xOffset; x <= 3 + xOffset; x++)
        {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                                                                 lastLadder + 1,
                                                                 ladderPos.posZ + z);
                int normalizedX = x - xOffset;
                int normalizedZ = z - zOffset;
                if (((Math.abs(normalizedX) == 2 && Math.abs(normalizedZ) < 3)
                     || (Math.abs(normalizedZ) == 2
                         && Math.abs(normalizedX) < 3))
                    && world.getBlock(curBlock.posX, curBlock.posY, curBlock.posZ) != Blocks.fence)
                {
                    setDelay(10);
                    if (missesItemsInInventory(new ItemStack(Blocks.fence)))
                    {
                        return true;
                    }
                    setBlockFromInventory(curBlock, Blocks.fence);
                    return true;
                }
            }
        }
        //Build torches
        for (int x = -3 + xOffset; x <= 3 + xOffset; x++)
        {
            for (int z = -3 + zOffset; z <= 3 + zOffset; z++)
            {
                if (x == 0 && 0 == z)
                {
                    continue;
                }
                ChunkCoordinates curBlock = new ChunkCoordinates(ladderPos.posX + x,
                                                                 lastLadder + 2,
                                                                 ladderPos.posZ + z);
                int normalizedX = x - xOffset;
                int normalizedZ = z - zOffset;
                if (Math.abs(normalizedX) == 2 && Math.abs(normalizedZ) == 2
                    && world.getBlock(curBlock.posX,
                                      curBlock.posY,
                                      curBlock.posZ)
                       != Blocks.torch)
                {
                    setDelay(10);
                    if (missesItemsInInventory(new ItemStack(Blocks.torch)))
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
        getOwnBuilding().currentLevel = getOwnBuilding().getLevels().size();
        //Send out update to client
        getOwnBuilding().markDirty();
        return false;
    }

    private void doShaftBuilding()
    {
        if (walkToBuilding())
        {
            if (buildNextBlockInShaft())
            {
                return;
            }
            getOwnBuilding().startingLevelShaft = 0;
            job.setStage(Stage.START_WORKING);
        }
    }

    private void doNodeMining()
    {
        Level currentLevel = getOwnBuilding().getCurrentLevel();
        if (currentLevel == null)
        {
            logger.warn("Current Level not set, resetting...");
            getOwnBuilding().currentLevel = getOwnBuilding().getLevels().size() - 1;
            return;
        }

        mineAtLevel(currentLevel);
    }

    private void mineAtLevel(Level currentLevel)
    {
        if (workingNode == null)
        {
            workingNode = findNodeOnLevel(currentLevel);
            return;
        }
        //Looking for a node to stand on while mining workingNode
        int foundDirection = 0;
        Node foundNode = null;
        List<Integer> directions = Arrays.asList(1, 2, 3, 4);

        for (Integer dir : directions)
        {
            Optional<Node> node = tryFindNodeInDirectionOfNode(currentLevel, workingNode, dir);
            if (node.isPresent() && getNodeStatusForDirection(node.get(), invertDirection(dir))
                                    == Node.NodeStatus.COMPLETED)
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
        int xoffset = getXDistance(foundDirection) / 2;
        int zoffset = getZDistance(foundDirection) / 2;
        if (xoffset > 0)
        {
            xoffset += 1;
        }
        else
        {
            xoffset -= 1;
        }
        if (zoffset > 0)
        {
            zoffset += 1;
        }
        else
        {
            zoffset -= 1;
        }
        ChunkCoordinates standingPosition = new ChunkCoordinates(workingNode.getX() + xoffset,
                                                                 currentLevel.getDepth(),
                                                                 workingNode.getZ() + zoffset);
        currentStandingPosition = standingPosition;
        if (workingNode.getStatus() == Node.NodeStatus.IN_PROGRESS
            || workingNode.getStatus() == Node.NodeStatus.COMPLETED
            || !walkToBlock(standingPosition))
        {
            mineNodeFromStand(workingNode, standingPosition, foundDirection);
        }
    }

    private boolean isOre(Block block)
    {
        //TODO make this more sophisticated
        return block instanceof BlockOre;
    }

    private boolean secureBlock(ChunkCoordinates curBlock, ChunkCoordinates safeStand)
    {
        if ((!getBlock(curBlock).getMaterial().blocksMovement()
             && getBlock(curBlock) != Blocks.torch)
            || isOre(getBlock(curBlock)))
        {

            if (!mineBlock(curBlock, safeStand))
            {
                //make securing go fast to not confuse the player
                setDelay(1);
                return false;
            }
            if (missesItemsInInventory(new ItemStack(Blocks.cobblestone)))
            {
                return false;
            }

            setBlockFromInventory(curBlock, Blocks.cobblestone);
            //To set it to clean stone... would be cheating
            return false;
        }
        return true;
    }


    private void mineNodeFromStand(Node minenode, ChunkCoordinates standingPosition, int direction)
    {

        //Check for safe Node
        for (int x = -NODE_DISTANCE / 2; x <= NODE_DISTANCE / 2; x++)
        {
            for (int z = -NODE_DISTANCE / 2; z <= NODE_DISTANCE / 2; z++)
            {
                for (int y = 0; y <= 5; y++)
                {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                                                     standingPosition.posY + y,
                                                                     minenode.getZ() + z);
                    if (((Math.abs(x) >= 2) && (Math.abs(z) >= 2))
                        || (getBlock(curBlock) != Blocks.air)
                        || (y < 1)
                        || (y > 4))
                    {
                        if (!secureBlock(curBlock, standingPosition))
                        {
                            return;
                        }
                    }
                }
            }
        }

        if (!mineSideOfNode(minenode, direction, standingPosition))
        {
            return;
        }

        if (minenode.getStatus() == Node.NodeStatus.AVAILABLE)
        {
            minenode.setStatus(Node.NodeStatus.IN_PROGRESS);
        }

        int xoffset = getXDistance(direction) / 2;
        int zoffset = getZDistance(direction) / 2;
        if (xoffset > 0)
        {
            xoffset -= 1;
        }
        else
        {
            xoffset += 1;
        }
        if (zoffset > 0)
        {
            zoffset -= 1;
        }
        else
        {
            zoffset += 1;
        }
        ChunkCoordinates newStandingPosition = new ChunkCoordinates(minenode.getX() + xoffset,
                                                                    standingPosition.posY,
                                                                    minenode.getZ() + zoffset);
        currentStandingPosition = newStandingPosition;


        if (minenode.getStatus() != Node.NodeStatus.COMPLETED)
        {
            //Mine middle
            for (int y = 1; y <= 4; y++)
            {
                for (int x = -1; x <= 1; x++)
                {
                    for (int z = -1; z <= 1; z++)
                    {
                        ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                                                         standingPosition.posY + y,
                                                                         minenode.getZ() + z);
                        if (getBlock(curBlock) == Blocks.torch
                            || getBlock(curBlock) == Blocks.planks
                            || getBlock(curBlock) == Blocks.fence)
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
            ChunkCoordinates sideStandingPosition = new ChunkCoordinates(minenode.getX() + getXDistance(dir) / 3,
                                                                         standingPosition.posY,
                                                                         minenode.getZ() + getZDistance(dir) / 3);
            currentStandingPosition = sideStandingPosition;
            if (!mineSideOfNode(minenode, dir, sideStandingPosition))
            {
                return;
            }
        }

        //Build middle
        //TODO: make it look nicer!
        if (!buildNodeSupportStructure(minenode, standingPosition))
        {
            return;
        }

        if (minenode.getStatus() == Node.NodeStatus.IN_PROGRESS)
        {
            minenode.setStatus(Node.NodeStatus.COMPLETED);
        }

        workingNode = null;
    }

    private boolean buildNodeSupportStructure(Node minenode, ChunkCoordinates standingPosition)
    {
        if (minenode.getStyle() == Node.NodeType.CROSSROAD)
        {
            return buildNodeCrossroadStructure(minenode, standingPosition);
        }
        if (minenode.getStyle() == Node.NodeType.BEND)
        {
            return buildNodeBendStructure(minenode, standingPosition);
        }
        if (minenode.getStyle() == Node.NodeType.TUNNEL)
        {
            return buildNodeTunnelStructure(minenode, standingPosition);
        }
        if (minenode.getStyle() == Node.NodeType.LADDER_BACK)
        {
            return true; //already done
        }
        logger.info("None of the above: " + minenode);
        return false;
    }

    private boolean buildNodeTunnelStructure(final Node minenode, final ChunkCoordinates standingPosition)
    {
        int direction = 3;
        if (minenode.getDirectionPosX() == Node.NodeStatus.WALL)
        {
            direction = 1;
        }

        for (int y = 4; y >= 1; y--)
        {
            for (int x = -2; x <= 2; x++)
            {
                for (int z = -2; z <= 2; z++)
                {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                                                     standingPosition.posY + y,
                                                                     minenode.getZ() + z);

                    Block material = null;
                    //Side pillars
                    if (Math.abs(x) == Math.abs(getXDistance(direction) / NODE_DISTANCE)
                        && Math.abs(z) == Math.abs(getZDistance(direction) / NODE_DISTANCE)
                        && y < 4)
                    {
                        material = Blocks.fence;
                    }
                    //Planks topping
                    if ((x == 0 || Math.abs(x) == Math.abs(getXDistance(direction) / NODE_DISTANCE))
                        && (z == 0 || Math.abs(z) == Math.abs(getZDistance(direction) / NODE_DISTANCE))
                        && y == 4)
                    {
                        material = Blocks.planks;
                    }
                    //torches at sides
                    if (((Math.abs(x) == 1 && Math.abs(z) == 0 && direction == 3)
                         || (Math.abs(x) == 0 && Math.abs(z) == 1 && direction == 1))
                        && y == 4
                        && getBlock(new ChunkCoordinates(minenode.getX(), standingPosition.posY + y, minenode.getZ()))
                           == Blocks.planks
                        && getBlock(curBlock) != Blocks.planks)
                    {
                        material = Blocks.torch;
                    }
                    if (material == null || getBlock(curBlock) == material)
                    {
                        if (material == null || getBlock(curBlock) == material)
                        {
                            continue;
                        }
                    }

                    if (missesItemsInInventory(new ItemStack(material)))
                    {
                        return false;
                    }

                    setBlockFromInventory(curBlock, material);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean buildNodeBendStructure(final Node minenode, final ChunkCoordinates standingPosition)
    {
        int directionx = 1;
        if (minenode.getDirectionPosX() == Node.NodeStatus.WALL)
        {
            directionx = 2;
        }
        int directionz = 3;
        if (minenode.getDirectionPosZ() == Node.NodeStatus.WALL)
        {
            directionz = 4;
        }

        for (int y = 4; y >= 1; y--)
        {
            for (int x = -3; x <= 3; x++)
            {
                for (int z = -3; z <= 3; z++)
                {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                                                     standingPosition.posY + y,
                                                                     minenode.getZ() + z);

                    Block material = null;
                    //Side pillars for x
                    if (x == (getXDistance(directionx) * 2) / NODE_DISTANCE
                        && Math.abs(z) == 1
                        && y < 4)
                    {
                        material = Blocks.fence;
                    }
                    //Side pillars for z
                    if (z == (getZDistance(directionz) * 2) / NODE_DISTANCE
                        && Math.abs(x) == 1
                        && y < 4)
                    {
                        material = Blocks.fence;
                    }

                    //Planks topping for x
                    if (x == (getXDistance(directionx) * 2) / NODE_DISTANCE
                        && Math.abs(z) <= 1
                        && y == 4)
                    {
                        material = Blocks.planks;
                    }
                    //Planks topping for z
                    if (z == (getZDistance(directionz) * 2) / NODE_DISTANCE
                        && Math.abs(x) <= 1
                        && y == 4)
                    {
                        material = Blocks.planks;
                    }

                    //torches at sides
                    if ((x == (getXDistance(directionx)) / NODE_DISTANCE)
                        && z == 0
                        && y == 4
                        && getBlock(new ChunkCoordinates(
                            minenode.getX() + (getXDistance(directionx) * 2) / NODE_DISTANCE,
                            standingPosition.posY + y,
                            minenode.getZ()))
                           == Blocks.planks)
                    {
                        material = Blocks.torch;
                    }
                    //torches at sides
                    if ((z == (getZDistance(directionz)) / NODE_DISTANCE)
                        && x == 0
                        && y == 4
                        && getBlock(new ChunkCoordinates(
                            minenode.getX(),
                            standingPosition.posY + y,
                            minenode.getZ() + (getZDistance(directionz) * 2) / NODE_DISTANCE))
                           == Blocks.planks)
                    {
                        material = Blocks.torch;
                    }

                    if (material == null || getBlock(curBlock) == material)
                    {
                        continue;
                    }

                    if (missesItemsInInventory(new ItemStack(material)))
                    {
                        return false;
                    }

                    setBlockFromInventory(curBlock, material);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean buildNodeCrossroadStructure(Node minenode, ChunkCoordinates standingPosition)
    {
        for (int y = 4; y >= 2; y--)
        {
            for (int x = -1; x <= 1; x++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                                                     standingPosition.posY + y,
                                                                     minenode.getZ() + z);

                    Block material = null;
                    //Middle top block and side stands
                    if (x == 0 && z == 0 && y == 4)
                    {
                        material = Blocks.fence;
                    }
                    //Planks topping
                    if (x == 0 && z == 0 && y == 3)
                    {
                        material = Blocks.planks;
                    }
                    //torches at sides
                    if (((Math.abs(x) == 1 && Math.abs(z) == 0) || (Math.abs(x) == 0 && Math.abs(z) == 1))
                        && y == 3
                        && getBlock(new ChunkCoordinates(minenode.getX(), standingPosition.posY + y, minenode.getZ()))
                           == Blocks.planks)
                    {
                        material = Blocks.torch;
                    }
                    if (material == null || getBlock(curBlock) == material)
                    {
                        continue;
                    }

                    if (missesItemsInInventory(new ItemStack(material)))
                    {
                        return false;
                    }

                    setBlockFromInventory(curBlock, material);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean mineSideOfNode(Node minenode, int directon, ChunkCoordinates standingPosition)
    {
        if (getNodeStatusForDirection(minenode, directon) == Node.NodeStatus.LADDER)
        {
            return true;
        }

        if (getNodeStatusForDirection(minenode, directon) == Node.NodeStatus.AVAILABLE)
        {
            setNodeStatusForDirection(minenode, directon, Node.NodeStatus.IN_PROGRESS);
        }

        int xoffset = getXDistance(directon) / 2;
        int zoffset = getZDistance(directon) / 2;
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
        for (int y = 1; y <= 4; y++)
        {
            for (int x = negx; x <= posx; x++)
            {
                for (int z = negz; z <= posz; z++)
                {
                    ChunkCoordinates curBlock = new ChunkCoordinates(minenode.getX() + x,
                                                                     standingPosition.posY + y,
                                                                     minenode.getZ() + z);
                    if (getBlock(curBlock) == Blocks.torch
                        || getBlock(curBlock) == Blocks.planks
                        || getBlock(curBlock) == Blocks.fence)
                    {
                        continue;
                    }
                    if (getNodeStatusForDirection(minenode, directon) == Node.NodeStatus.WALL)
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
        if (getNodeStatusForDirection(minenode, directon) == Node.NodeStatus.IN_PROGRESS)
        {
            setNodeStatusForDirection(minenode, directon, Node.NodeStatus.COMPLETED);
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
        return start.getX() + getXDistance(direction) == check.getX() && start.getZ() + getZDistance(direction) == check
                .getZ();
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
        return new ArrayList<>(curlevel.getNodes()).parallelStream().filter(check -> isNodeInDirectionOfOtherNode(
                finalCurrentNode,
                direction,
                check)).findFirst();
    }

    private Node createNewNodeInDirectionFromNode(Node start, int direction)
    {
        int x = start.getX() + getXDistance(direction);
        int z = start.getZ() + getZDistance(direction);
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
        Node currentNode = currentLevel.getLadderNode();
        LinkedList<Node> visited = new LinkedList<>();
        while (currentNode != null)
        {
            if (visited.contains(currentNode))
            {
                return null;
            }

            visited.add(currentNode);
            if (currentNode.getStatus() == Node.NodeStatus.AVAILABLE
                || currentNode.getStatus() == Node.NodeStatus.IN_PROGRESS)
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


    @Override
    public void workOnTask()
    {

        //Miner wants to work but is not at building
        if (job.getStage() == Stage.START_WORKING)
        {
            if (walkToBuilding())
            {
                return;
            }
            //Miner is at building
            job.setStage(Stage.PREPARING);
            return;
        }

        //Miner is at building and prepares for work
        if (job.getStage() == Stage.PREPARING)
        {
            if (!getOwnBuilding().foundLadder)
            {
                job.setStage(Stage.SEARCHING_LADDER);
                return;
            }

            job.setStage(Stage.CHECK_MINESHAFT);
        }

        //Looking for the ladder to walk to
        if (job.getStage() == Stage.SEARCHING_LADDER)
        {
            lookForLadder();
            return;
        }

        //Walking to the ladder to check out the mine
        if (job.getStage() == Stage.LADDER_FOUND)
        {
            if (walkToLadder())
            {
                return;
            }
            job.setStage(Stage.CHECK_MINESHAFT);
        }

        //Standing on top of the ladder, checking out mine
        if (job.getStage() == Stage.CHECK_MINESHAFT)
        {
            //TODO: check if mineshaft needs repairing!

            //Check if we reached the mineshaft depth limit
            if (getLastLadder(getOwnBuilding().ladderLocation) < getOwnBuilding().getDepthLimit())
            {
                job.setStage(Stage.MINING_NODE);
                getOwnBuilding().clearedShaft = true;
                return;
            }
            job.setStage(Stage.MINING_SHAFT);
            getOwnBuilding().clearedShaft = false;
            return;
        }

        if (job.getStage() == Stage.MINING_SHAFT)
        {

            doShaftMining();
            return;
        }

        if (job.getStage() == Stage.BUILD_SHAFT)
        {
            doShaftBuilding();
            return;
        }

        if (job.getStage() == Stage.MINING_NODE)
        {
            doNodeMining();
            return;
        }

        logger.info("[" + job.getStage() + "] Stopping here, old code ahead...");
        setDelay(100);
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
    }

    private int getBlockMiningDelay(Block block, ChunkCoordinates chunkCoordinates)
    {
        return getBlockMiningDelay(block, chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getBlockMiningDelay(Block block, int x, int y, int z)
    {
        if (worker.getHeldItem() == null)
        {
            return (int) block.getBlockHardness(world, x, y, z);
        }
        return (int) (50 * block.getBlockHardness(world, x, y, z) / (worker.getHeldItem()
                                                                           .getItem()
                                                                           .getDigSpeed(worker.getHeldItem(),
                                                                                        block,
                                                                                        0)));
    }

    private void setBlockFromInventory(ChunkCoordinates location, Block block)
    {
        setBlockFromInventory(location, block, 0);
    }

    private void setBlockFromInventory(ChunkCoordinates location, Block block, int metadata)
    {
        int slot = worker.findFirstSlotInInventoryWith(block);
        if (slot != -1)
        {
            worker.getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
            world.setBlock(location.posX, location.posY, location.posZ, block, metadata, 3);
        }
    }

    private int getBlockMetadata(ChunkCoordinates loc)
    {
        return world.getBlockMetadata(loc.posX, loc.posY, loc.posZ);
    }

    private Block getBlock(ChunkCoordinates loc)
    {
        return world.getBlock(loc.posX, loc.posY, loc.posZ);
    }

    private int getLastLadder(ChunkCoordinates chunkCoordinates)
    {
        return getLastLadder(chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getLastLadder(int x, int y, int z)
    {
        if (world.getBlock(x, y, z).isLadder(world, x, y, z, null))
        {
            return getLastLadder(x, y - 1, z);
        }
        else
        {
            return y + 1;
        }
    }

    private int getFirstLadder(ChunkCoordinates chunkCoordinates)
    {
        return getFirstLadder(chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ);
    }

    private int getFirstLadder(int x, int y, int z)
    {
        if (world.getBlock(x, y, z).isLadder(world, x, y, z, null))
        {
            return getFirstLadder(x, y + 1, z);
        }
        else
        {
            return y - 1;
        }
    }

    public enum Stage
    {
        INVENTORY_FULL,
        SEARCHING_LADDER,
        MINING_VEIN,
        MINING_SHAFT,
        START_WORKING,
        MINING_NODE,
        PREPARING,
        START_MINING,
        LADDER_FOUND,
        CHECK_MINESHAFT,
        BUILD_SHAFT,
        FILL_VEIN
    }
}