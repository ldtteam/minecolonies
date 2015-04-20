package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ModClassLoader;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Colton, Raycoms
 */

public class EntityAIWorkMiner extends EntityAIWork<JobMiner>
{
    private static Logger logger = LogManager.getLogger("Miner");
    public enum Stage
    {
        INSUFFICIENT_TOOLS,
        INSUFFICIENT_BLOCKS,
        INVENTORY_FULL,
        SEARCHING_LADDER,
        MINING_VEIN,
        MINING_SHAFT,
        WORKING,
        NEED_HIGHER_TOOLS,
        MINING_NODE,
        FILL_VEIN
    }

    private int delay = 0;                   //Must be saved here
    private String NEED_ITEM;

    public List<ChunkCoordinates> localVein;

    public ChunkCoordinates getLocation;
    private double baseSpeed = 1;
    private int tryThreeTimes = 3;
    private boolean hasDelayed = false;
    private int currentY=200;                //Can be saved here
    private int clear = 1;                   //Can be saved here for now
    private int blocksMined = 0;
    private Block needBlock = Blocks.dirt;
    private Item needItem;
    private Block hasToMine = Blocks.cobblestone;
    private ChunkCoordinates miningBlock;
    ChunkCoordinates loc;
    private int clearNode=0;
    private int canMineNode=0;
    private int currentLevel=-1;
    
    //Job class -> Needitem
    public EntityAIWorkMiner(JobMiner job)
    {
        super(job);

    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute();
    }

    @Override
    public void startExecuting()
    {
        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    @Override
    public void updateTask()
    {
        BuildingMiner b = (BuildingMiner)(worker.getWorkBuilding());
        if(b == null){return;}

        if(currentLevel == -1)
        {
            currentLevel = b.currentLevel;
        }

        if((b.ladderLocation == null ||  b.shaftStart == null) && job.getStage() != Stage.MINING_NODE)
        {
                if (tryThreeTimes < 1)
                {
                    b.foundLadder = false;
                    job.setStage(Stage.SEARCHING_LADDER);
                } else {
                    tryThreeTimes--;
                    return;
                }
        }
        else if((b.ladderLocation.equals(new ChunkCoordinates(0, 0, 0)) ||  b.shaftStart.equals(new ChunkCoordinates(0,0,0))) && job.getStage() != Stage.MINING_NODE)
        {
            if (tryThreeTimes < 1)
            {
                b.foundLadder = false;
                job.setStage(Stage.SEARCHING_LADDER);
            }
            else
            {
                tryThreeTimes--;
                return;
            }
        }
        else if(b.ladderLocation!=null && (job.getStage() == Stage.MINING_NODE || job.getStage() == Stage.WORKING))
        {
            if (b.ladderLocation.posY - 1 > b.getMaxY())
            {
                b.clearedShaft = false;
                job.setStage(Stage.MINING_SHAFT);
                //If level = +/- long ago, build on y or -1
            }
        }

        if (delay > 0)
        {
            if(miningBlock != null && (job.getStage() == Stage.MINING_NODE || job.getStage() == Stage.MINING_SHAFT || job.getStage() == Stage.MINING_VEIN))
            {
                int x = miningBlock.posX;
                int y = miningBlock.posY;
                int z = miningBlock.posZ;

                worker.swingItem();
                try
                {
                    FMLClientHandler.instance().getClient().effectRenderer.addBlockHitEffects(x, y, z, 1);
                }
                catch(Exception e)
                {
                    logger.info("Couldn't add effect");
                }
            }
            delay--;
        }
        else
        {
            switch (job.getStage())
            {
                case MINING_NODE:
                    if(b.levels!=null)
                    {
                        if(b.startingLevelNode == 5)
                        {
                            if(canMineNode < 1)
                            {
                                //12 fences == 29 planks + 1 Torch  -> 3 Nodes

                                if (inventoryContainsMany(b.floorBlock) >= 30 && (inventoryContains(Items.coal) != -1 || inventoryContainsMany(Blocks.torch) >= 3))
                                {
                                    canMineNode = 3;
                                }
                                else
                                {
                                    if (inventoryContains(Items.coal) == -1 && inventoryContainsMany(Blocks.torch) < 3)
                                    {
                                        needItem = Items.coal;
                                    }
                                    else
                                    {
                                        needBlock = b.floorBlock;
                                    }
                                    job.setStage(Stage.INSUFFICIENT_BLOCKS);

                                }
                            }
                            else
                            {
                                mineNode(b);
                            }

                        }
                        else
                        {
                            mineNode(b);
                        }
                    }
                    else
                    {
                        createShaft(b.vectorX, b.vectorZ);
                    }
                    break;
                case INSUFFICIENT_TOOLS:
                    if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                    {
                        logger.info("Need tools");

                        delay = 30;

                        if (needItem == null)
                        {
                            job.setStage(Stage.WORKING);
                            return;
                        }
                        else
                        {
                            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeedTools", needItem.getUnlocalizedName());

                            isInHut(needItem);
                            if (hasAllTheTools())
                            {
                                needItem = null;
                                job.setStage(Stage.WORKING);
                                return;
                            }
                        }
                    }

                    break;
                case INSUFFICIENT_BLOCKS:
                    if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                    {
                            if (needBlock == null && needItem != null)
                            {
                                logger.info("Need: " + needItem.getUnlocalizedName());
                                LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeed", needItem.getUnlocalizedName());
                            }
                            else if (needItem == null && needBlock != null)
                            {
                                logger.info("Need: " + needBlock.getUnlocalizedName());
                                LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeed", needBlock.getUnlocalizedName());

                            }
                            else if (needItem == null)
                            {
                                logger.info("Need nothing");

                                job.setStage(Stage.WORKING);
                                return;
                            }
                            else
                            {
                                logger.info("Need: " + needBlock.getUnlocalizedName() + needItem.getUnlocalizedName());
                                LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeed", needItem.getUnlocalizedName());
                                LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeed", needBlock.getUnlocalizedName());

                            }

                            delay = 30;
                            isInHut(needBlock);
                            isInHut(needItem);

                            boolean hasBlock = inventoryContains(needBlock) != -1;
                            boolean hasItem = inventoryContains(needItem) != -1;

                            if (hasBlock || hasItem) {
                                needBlock = null;
                                needItem = null;
                                job.setStage(Stage.WORKING);
                            }

                    }
                    break;
                case INVENTORY_FULL:
                    dumpInventory();
                    break;
                case SEARCHING_LADDER:
                    findLadder();
                    break;
                case MINING_VEIN:
                    mineVein();
                    break;
                case FILL_VEIN:
                    fillVein();
                    break;
                case MINING_SHAFT:
                    createShaft(b.vectorX, b.vectorZ);
                    break;
                case WORKING:
                    if (!b.foundLadder)
                    {
                        job.setStage(Stage.SEARCHING_LADDER);
                    }
                    else if(b.activeNode != null)
                    {
                        job.setStage(Stage.MINING_NODE);
                    }
                    else if (!b.clearedShaft)
                    {
                        job.setStage(Stage.MINING_SHAFT);
                    }
                    else
                    {
                        job.setStage(Stage.MINING_NODE);
                    }

                    break;
                case NEED_HIGHER_TOOLS:
                    if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                    {
                        logger.info("Need better Tools");
                        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.miner.messageNeedBetterTools");


                        delay = 30;

                            if (hasAllTheTools())
                            {
                                job.setStage(Stage.WORKING);
                            }
                    }
                    break;
            }
        }
    }

    private int  unsignVector(int i)
    {
        if(i == 0)
        {
            return 0;
        }

        return 1;
    }

    private void mineNode(BuildingMiner b)
    {
        if(b.levels.size()<currentLevel)
        {
            b.activeNode = null;
            job.setStage(Stage.MINING_SHAFT);
            return;
        }

        if (inventoryContainsMany(b.floorBlock)<= 10 || (inventoryContainsMany(Blocks.torch)<3 && inventoryContains(Items.coal)==-1))
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            canMineNode = 0;
            needBlock = b.floorBlock;
            return;
        }

        if(b.levels.get(currentLevel).getNodes().size() == 0)
        {
            b.currentLevel++;
            currentLevel++;
            return;
        }

        int depth = b.levels.get(currentLevel).getDepth();

        if(b.activeNode == null || b.activeNode.getStatus() == Node.Status.COMPLETED || b.activeNode.getStatus() == Node.Status.AVAILABLE)
        {
            currentLevel = b.currentLevel;
            int rand1 = (int)(Math.random()*3);
            int randomNum;

            if(b.levels.get(currentLevel).getNodes() == null)
            {
                if(b.levels.size()<currentLevel+1)
                {
                    b.activeNode = null;
                    job.setStage(Stage.MINING_SHAFT);
                    return;
                }
                else
                {
                    b.currentLevel++;
                    currentLevel++;
                    return;
                }
            }
            else if(rand1 != 1)
            {
                randomNum = b.levels.get(currentLevel).getNodes().size();

                if(b.levels.get(currentLevel).getNodes().size()<b.active)
                {
                    return;
                }
            }
            else
            {
                 randomNum = (int) (Math.random() * b.levels.get(currentLevel).getNodes().size());
            }

            if(b.levels.get(currentLevel).getNodes().size() > randomNum)
            {
                Node node = b.levels.get(currentLevel).getNodes().get(randomNum);

                if (node.getStatus() == Node.Status.AVAILABLE)
                {

                    int x = node.getID().getX();
                    int y = b.levels.get(currentLevel).getDepth();
                    int z = node.getID().getY();
                    Block block = world.getBlock(x,y,z);

                    if(b.activeNode!=null && job.getStage() == Stage.MINING_NODE && (block.isAir(world,x+node.getVectorX(),y,z+node.getVectorZ()) || !canWalkOn(x+node.getVectorX(),y,z+node.getVectorZ())))
                    {
                        logger.info("Removed Node because of Air Node: " + b.active + " x: " + x + " z: " + z + " vectorX: " + b.activeNode.getVectorX() + " vectorZ: " + b.activeNode.getVectorZ());
                        b.levels.get(currentLevel).getNodes().remove(randomNum);
                        return;
                    }

                    if (node.getID().getX() > b.shaftStart.posX + b.getMaxX() || node.getID().getY() > b.shaftStart.posZ + b.getMaxZ() || node.getID().getX() < b.shaftStart.posX - b.getMaxX() || node.getID().getY() < b.shaftStart.posZ - b.getMaxZ())
                    {
                        b.levels.get(currentLevel).getNodes().remove(randomNum);
                        return;
                    }

                    logger.info("Starting Node: " + randomNum);

                    loc = new ChunkCoordinates(node.getID().getX(), depth, node.getID().getY());
                    b.activeNode = node;
                    b.active = randomNum;
                    b.activeNode.setStatus(Node.Status.IN_PROGRESS);
                    clearNode = 0;
                    b.startingLevelNode = 0;
                    b.markDirty();
                }
            }
        }
        else if(b.activeNode.getStatus() == Node.Status.IN_PROGRESS)
        {
            if(loc == null)
            {
                loc = new ChunkCoordinates(b.activeNode.getID().getX(), depth, b.activeNode.getID().getY());
            }

            if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, new ChunkCoordinates(loc.posX-b.activeNode.getVectorX(), loc.posY, loc.posZ-b.activeNode.getVectorZ())))
            {
                Block block;
                int uVX = 0;
                int uVZ = 0;

                if (b.startingLevelNode == 5)
                {
                    b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
                    b.activeNode.setStatus(Node.Status.COMPLETED);
                    b.levels.get(currentLevel).getNodes().remove(b.active);


                    if (b.activeNode.getVectorX() == 0)
                    {
                        if(!world.isAirBlock(b.activeNode.getID().getX() + 2,b.levels.get(currentLevel).getDepth(), b.activeNode.getID().getY() + 4 * b.activeNode.getVectorZ()))
                        {
                            b.levels.get(currentLevel).addNewNode(b.activeNode.getID().getX() + 2, b.activeNode.getID().getY() + 4 * b.activeNode.getVectorZ(), unsignVector(b.activeNode.getVectorZ()), unsignVector(b.activeNode.getVectorX()));
                        }
                        if(!world.isAirBlock(b.activeNode.getID().getX() - 2,b.levels.get(currentLevel).getDepth(), b.activeNode.getID().getY() + 4 * b.activeNode.getVectorZ()))
                        {
                            b.levels.get(currentLevel).addNewNode(b.activeNode.getID().getX() - 2, b.activeNode.getID().getY() + 4 * b.activeNode.getVectorZ(), -unsignVector(b.activeNode.getVectorZ()), -unsignVector(b.activeNode.getVectorX()));
                        }
                    }
                    else
                    {
                        if(!world.isAirBlock(b.activeNode.getID().getX() + 4,b.levels.get(currentLevel).getDepth(), b.activeNode.getID().getY() + 2 * b.activeNode.getVectorZ()))
                        {
                            b.levels.get(currentLevel).addNewNode(b.activeNode.getID().getX() + 4 * b.activeNode.getVectorX(), b.activeNode.getID().getY() + 2, unsignVector(b.activeNode.getVectorZ()), unsignVector(b.activeNode.getVectorX()));
                        }
                        if(!world.isAirBlock(b.activeNode.getID().getX() + 4,b.levels.get(currentLevel).getDepth(), b.activeNode.getID().getY() -2 * b.activeNode.getVectorZ()))
                        {
                            b.levels.get(currentLevel).addNewNode(b.activeNode.getID().getX() + 4 * b.activeNode.getVectorX(), b.activeNode.getID().getY() - 2, -unsignVector(b.activeNode.getVectorZ()), -unsignVector(b.activeNode.getVectorX()));
                        }
                    }

                    if(!world.isAirBlock((b.activeNode.getID().getX() + 5 * b.activeNode.getVectorX()),b.levels.get(currentLevel).getDepth(),b.activeNode.getID().getY() + 5 * b.activeNode.getVectorZ()))
                    {
                        b.levels.get(currentLevel).addNewNode(b.activeNode.getID().getX() + 5 * b.activeNode.getVectorX(), b.activeNode.getID().getY() + 5 * b.activeNode.getVectorZ(), b.activeNode.getVectorX(), b.activeNode.getVectorZ());
                    }
                    logger.info("Finished Node: " + b.active);

                    b.markDirty();
                }
                else
                {
                    if (b.activeNode.getVectorX() == 0)
                    {
                        uVX = 1;
                    }
                    else
                    {
                        uVZ = 1;
                    }

                    switch (clearNode)
                    {
                        case 0:
                            block = world.getBlock(loc.posX, loc.posY + 1, loc.posZ);
                            checkAbove(loc.posX, loc.posY + 2, loc.posZ);
                            if (doMining(block, loc.posX, loc.posY + 1, loc.posZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 1:
                            block = world.getBlock(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ);
                            checkAbove(loc.posX - uVX, loc.posY + 2, loc.posZ - uVZ);
                            if (!isALiquid(loc.posX - 2 * uVX, loc.posY + 1, loc.posZ - 2 * uVZ))
                            {
                                setBlockFromInventory(loc.posX - 2 * uVX, loc.posY + 1, loc.posZ - 2 * uVZ, Blocks.cobblestone);
                            }
                            if (doMining(block, loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 2:
                            block = world.getBlock(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ);
                            checkAbove(loc.posX + uVX, loc.posY + 2, loc.posZ + uVZ);
                            if (!isALiquid(loc.posX + 2 * uVX, loc.posY + 1, loc.posZ + 2 * uVZ))
                            {
                                setBlockFromInventory(loc.posX + 2 * uVX, loc.posY + 1, loc.posZ + 2 * uVZ, Blocks.cobblestone);
                            }
                            if (doMining(block, loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 3:
                            block = world.getBlock(loc.posX + uVX, loc.posY, loc.posZ + uVZ);
                            if (!isALiquid(loc.posX + 2 * uVX, loc.posY, loc.posZ + 2 * uVZ))
                            {
                                setBlockFromInventory(loc.posX + 2 * uVX, loc.posY, loc.posZ + 2 * uVZ, Blocks.cobblestone);
                            }
                            if (doMining(block, loc.posX + uVX, loc.posY, loc.posZ + uVZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 4:
                            block = world.getBlock(loc.posX, loc.posY, loc.posZ);
                            if (doMining(block, loc.posX, loc.posY, loc.posZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 5:
                            block = world.getBlock(loc.posX - uVX, loc.posY, loc.posZ - uVZ);
                            if (!isALiquid(loc.posX - 2 * uVX, loc.posY, loc.posZ - 2 * uVZ))
                            {
                                setBlockFromInventory(loc.posX - 2 * uVX, loc.posY, loc.posZ - 2 * uVZ, Blocks.cobblestone);
                            }
                            if (doMining(block, loc.posX - uVX, loc.posY, loc.posZ - uVZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 6:
                            block = world.getBlock(loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ);
                            checkUnder(loc.posX + uVX, loc.posY - 2, loc.posZ + uVZ);
                            if (!isALiquid(loc.posX - 2 * uVX, loc.posY - 1, loc.posZ - 2 * uVZ))
                            {
                                setBlockFromInventory(loc.posX - 2 * uVX, loc.posY - 1, loc.posZ - 2 * uVZ, Blocks.cobblestone);
                            }
                            if (doMining(block, loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 7:
                            block = world.getBlock(loc.posX, loc.posY - 1, loc.posZ);
                            checkUnder(loc.posX + uVX, loc.posY - 2, loc.posZ + uVZ);
                            if (doMining(block, loc.posX, loc.posY - 1, loc.posZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 8:
                            block = world.getBlock(loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ);
                            checkUnder(loc.posX, loc.posY - 2, loc.posZ);
                            if (!isALiquid(loc.posX + 2 * uVX, loc.posY - 1, loc.posZ + 2 * uVZ))
                            {
                                setBlockFromInventory(loc.posX + 2 * uVX, loc.posY - 1, loc.posZ + 2 * uVZ, Blocks.cobblestone);
                            }
                            if (doMining(block, loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ))
                            {
                                clearNode += 1;
                            }
                            break;
                        case 9:

                            if (b.startingLevelNode == 2)
                            {
                                int neededPlanks = 10;
                                canMineNode -= 1;

                                world.setBlock(loc.posX, loc.posY + 1, loc.posZ, Blocks.planks);
                                world.setBlock(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ, Blocks.planks);
                                world.setBlock(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ, Blocks.planks);
                                world.setBlock(loc.posX + uVX, loc.posY, loc.posZ + uVZ, Blocks.fence);
                                world.setBlock(loc.posX - uVX, loc.posY, loc.posZ - uVZ, Blocks.fence);
                                world.setBlock(loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ, Blocks.fence);
                                world.setBlock(loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ, Blocks.fence);

                                int meta = 0;

                                if (b.activeNode.getVectorZ() < 0)
                                {
                                    meta = 3;
                                } else if (b.activeNode.getVectorZ() > 0)
                                {
                                    meta = 4;
                                }
                                else if (b.activeNode.getVectorX() < 0)
                                {
                                    meta = 1;
                                }
                                else if (b.activeNode.getVectorX() > 0)
                                {
                                    meta = 2;
                                }

                                while (neededPlanks > 0)
                                {
                                    int slot = inventoryContains(b.floorBlock);
                                    int size = worker.getInventory().getStackInSlot(slot).stackSize;

                                    if (size > neededPlanks)
                                    {
                                        worker.getInventory().decrStackSize(slot, 10);
                                        neededPlanks = 0;

                                    }
                                    else
                                    {
                                        worker.getInventory().decrStackSize(slot, size);
                                        neededPlanks -= size;
                                    }
                                }

                                if (inventoryContainsMany(Items.coal) > 0)
                                {
                                    int slot = inventoryContains(Items.coal);
                                    worker.getInventory().decrStackSize(slot, 1);
                                }
                                else if (inventoryContainsMany(Blocks.torch) > 0)
                                {
                                    int slot = inventoryContains(Blocks.torch);
                                    worker.getInventory().decrStackSize(slot, 1);
                                }

                                world.setBlock(loc.posX - b.activeNode.getVectorX(), loc.posY + 1, loc.posZ - b.activeNode.getVectorZ(), Blocks.torch, meta, 0x3);

                            }
                            b.startingLevelNode += 1;
                            loc.set(loc.posX + b.activeNode.getVectorX(), loc.posY, loc.posZ + b.activeNode.getVectorZ());

                            clearNode = 0;
                            b.markDirty();
                            break;
                    }
                }
            }
        }
    }

    private boolean isALiquid(int x, int y, int z)
    {
       return !world.getBlock(x,y,z).getMaterial().isLiquid();
    }

    private void checkAbove(int x,int y,int z)
    {
        Block blockAbove = world.getBlock(x, y, z);
        isValuable(x,y,z);
        if(blockAbove == Blocks.sand || blockAbove == Blocks.gravel || !canWalkOn(x,y,z))
        {
           setBlockFromInventory(x,y,z,Blocks.cobblestone);
        }
    }

    private void checkUnder(int x,int y,int z)
    {
        isValuable(x,y,z);
        if(!canWalkOn(x,y,z))
        {
            setBlockFromInventory(x,y,z,Blocks.cobblestone);
        }
    }

    private boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getItem().getToolClasses(null /* not used */).contains("pickaxe") || stack.getItem().getToolClasses(null /* not used */).contains("shovel"));
    }

    private boolean isInHut(Block block)
    {
        if(worker.getWorkBuilding().getTileEntity()==null)
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            needBlock = block;
            return false;
        }

        int size = worker.getWorkBuilding().getTileEntity().getSizeInventory();

        for(int i = 0; i < size; i++)
        {
            ItemStack stack = worker.getWorkBuilding().getTileEntity().getStackInSlot(i);
            if(stack != null && stack.getItem() instanceof ItemBlock)
            {
                    Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                    if(content.equals(block))
                    {
                        needBlock = null;
                        ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);

                        if (returnStack == null)
                        {
                            worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize);
                        }
                        else
                        {
                            worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }

                        return true;
                    }
            }
        }
        return false;
    }

    private boolean isInHut(Item item)
    {
        if(isInHut(Blocks.torch) && item == Items.coal)
        {
            return true;
        }

        if(worker.getWorkBuilding().getTileEntity()==null)
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            needItem = item;
            return false;
        }

        int size = worker.getWorkBuilding().getTileEntity().getSizeInventory();

        for(int i = 0; i < size; i++)
        {
            ItemStack stack = worker.getWorkBuilding().getTileEntity().getStackInSlot(i);
            if(stack != null)
            {
                Item content = stack.getItem();
                if(content.equals(item) || content.getToolClasses(null /* not used */).contains(NEED_ITEM))
                {
                    ItemStack returnStack = InventoryUtils.setStack(worker.getInventory(), stack);
                    needItem = null;
                    if (returnStack == null)
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize);
                    }
                    else
                    {
                        worker.getWorkBuilding().getTileEntity().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void dumpInventory()
    {
        if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
           {
                for (int i = 0; i < worker.getInventory().getSizeInventory(); i++)
                {
                    ItemStack stack = worker.getInventory().getStackInSlot(i);
                    if (stack != null && !isStackTool(stack))
                    {
                        if (worker.getWorkBuilding().getTileEntity() != null)
                        {
                            ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);
                            if (returnStack == null)
                            {
                                worker.getInventory().decrStackSize(i, stack.stackSize);
                            }
                            else
                            {
                                worker.getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                            }
                        }
                    }
                }
               job.setStage(Stage.WORKING);
               blocksMined = 0;
            }
    }

    private void mineVein()
    {
        BuildingMiner b = (BuildingMiner)worker.getWorkBuilding();
        if (b == null) return;
        if(job.vein == null)
        {
            job.setStage(Stage.WORKING);
            return;
        }

        if(job.vein.size() == 0)
        {
            job.vein = null;
            job.veinId = 0;
            job.setStage(Stage.FILL_VEIN);

        }
        else
        {
            if(localVein==null)
            {
                localVein = new ArrayList<ChunkCoordinates>();
            }

            ChunkCoordinates nextLoc = job.vein.get(0);
            localVein.add(nextLoc);
            Block block = ChunkCoordUtils.getBlock(world, nextLoc);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;

            if(!doMining(block, nextLoc.posX, nextLoc.posY, nextLoc.posZ))
            {
                return;
            }

            job.vein.remove(0);

            if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
            {
                setBlockFromInventory(x, y-1, z, Blocks.dirt);
            }
            if (world.getBlock(x,y,z) == Blocks.sand || world.getBlock(x,y,z) == Blocks.gravel)
            {
                setBlockFromInventory(x, y + 1, z, Blocks.dirt);
            }

            avoidCloseLiquid(x,y,z);
        }
    }

    private void fillVein()
    {
        BuildingMiner b = (BuildingMiner)worker.getWorkBuilding();
        if (b == null) return;

        if(localVein.size() == 0)
        {
            localVein = null;
            job.setStage(Stage.WORKING);
        }
        else
        {
            ChunkCoordinates nextLoc = localVein.get(0);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;
            localVein.remove(0);

            if (world.isAirBlock(x, y, z) || !canWalkOn(x, y , z))
            {
                setBlockFromInventory(x,y,z,Blocks.cobblestone);
            }
        }
    }

    private void avoidCloseLiquid(int centerX, int centerY, int centerZ)
    {
        for (int x = centerX - 3; x <= centerX + 3; x++)
        {
            for (int z = centerZ - 3; z <= centerZ + 3; z++)
            {
                for (int y = centerY ; y <= centerY + 2; y++)
                {
                    Block block = world.getBlock(x,y,z);

                    if (block.getMaterial().isLiquid())
                    {
                        setBlockFromInventory(x, y, z, Blocks.dirt);
                    }
                }
            }
        }
    }

    private boolean hasAllTheTools()
    {
        boolean hasPickAxeInHand;
        boolean hasSpadeInHand;

        if (worker.getHeldItem() == null)
        {
            hasPickAxeInHand = false;
            hasSpadeInHand = false;
        }
        else
        {
            hasPickAxeInHand = worker.getHeldItem().getItem().getToolClasses(null /* not used */).contains("pickaxe");
            hasSpadeInHand = worker.getHeldItem().getItem().getToolClasses(null /* not used */).contains("shovel");
        }

        int hasSpade = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel");
        int hasPickAxe = InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "pickaxe");


        boolean Spade = hasSpade > -1 || hasSpadeInHand;
        boolean Pickaxe = hasPickAxeInHand || hasPickAxe > -1;

        if(!Spade)
        {
            needItem = Items.iron_shovel;
            NEED_ITEM = "shovel";
            job.setStage(Stage.INSUFFICIENT_TOOLS);
        }
        else if (!Pickaxe)
        {
            needItem = Items.iron_pickaxe;
            NEED_ITEM = "pickaxe";
            job.setStage(Stage.INSUFFICIENT_TOOLS);
        }


        if(!Pickaxe || !Spade)
        {
            return false;
        }


        boolean canMine =  false;

        if(hasToMine == Blocks.air || hasToMine == Blocks.fence  || hasToMine == Blocks.planks || hasToMine == Blocks.ladder)
        {
            canMine = true;
        }
        if(!canMine)
        {
            canMine = ForgeHooks.canToolHarvestBlock(hasToMine, 0, worker.getHeldItem());
        }
        if(!canMine)
        {
            if(hasPickAxeInHand)
            {
                holdShovel();
            }
            else
            {
                holdPickAxe();
            }
            canMine = ForgeHooks.canToolHarvestBlock(hasToMine, 0, worker.getHeldItem());


        }
        if(!canMine)
        {
            job.setStage(Stage.INSUFFICIENT_TOOLS);
        }
             return canMine;
    }

    void holdShovel()
    {
        worker.setHeldItem(InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "shovel"));
    }

    void holdPickAxe()
    {
        worker.setHeldItem(InventoryUtils.getFirstSlotContainingTool(worker.getInventory(), "pickaxe"));
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

    private void findLadder()
    {
        BuildingMiner b = (BuildingMiner)worker.getWorkBuilding();
        if (b == null) return;



        int posX = worker.getWorkBuilding().getLocation().posX;
        int posY = worker.getWorkBuilding().getLocation().posY + 2;
        int posZ = worker.getWorkBuilding().getLocation().posZ;

        for (int x = posX - 10; x < posX + 10; x++)
        {
            for (int z = posZ - 10; z < posZ + 10; z++)
            {
                for (int y = posY - 10; y < posY; y++)
                {
                    if (b.foundLadder)
                    {
                        job.setStage(Stage.MINING_SHAFT);
                        return;
                    }
                    else
                    if (world.getBlock(x, y, z).equals(Blocks.ladder))//Parameters unused
                    {
                        int lastY = getLastLadder(x, y, z);
                        b.ladderLocation = new ChunkCoordinates(x, lastY, z);
                        logger.info("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                        delay = 10;

                        if(getLocation == null)
                        {
                            getLocation = new ChunkCoordinates(b.ladderLocation.posX,b.ladderLocation.posY,b.ladderLocation.posZ);
                        }
                        if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation)) {
                            b.cobbleLocation = new ChunkCoordinates(x, lastY, z);

                            if (world.getBlock(b.ladderLocation.posX - 1, b.ladderLocation.posY, b.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x - 1, lastY, z);
                                b.vectorX = 1;
                                b.vectorZ = 0;
                                logger.info("Found cobble - West");
                                //West
                            }
                            else if (world.getBlock(b.ladderLocation.posX + 1, b.ladderLocation.posY, b.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x + 1, lastY, z);
                                b.vectorX = -1;
                                b.vectorZ = 0;
                                logger.info("Found cobble - East");
                                //East
                            }
                            else if (world.getBlock(b.ladderLocation.posX, b.ladderLocation.posY, b.ladderLocation.posZ - 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x, lastY, z - 1);
                                b.vectorZ = 1;
                                b.vectorX = 0;
                                logger.info("Found cobble - South");
                                //South
                            }
                            else if (world.getBlock(b.ladderLocation.posX, b.ladderLocation.posY, b.ladderLocation.posZ + 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                b.cobbleLocation = new ChunkCoordinates(x, lastY, z + 1);
                                b.vectorZ = -1;
                                b.vectorX = 0;
                                logger.info("Found cobble - North");
                                //North
                            }
                            //world.setBlockToAir(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                            getLocation = new ChunkCoordinates(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ);
                            b.shaftStart = new ChunkCoordinates(b.ladderLocation.posX, b.ladderLocation.posY - 1, b.ladderLocation.posZ);
                            b.foundLadder = true;
                            hasAllTheTools();
                            job.setStage(Stage.WORKING);
                            b.markDirty();

                        }
                    }
                }
            }
        }
    }

    private void createShaft(int vektorX, int vektorZ)
    {
        BuildingMiner b = (BuildingMiner)worker.getWorkBuilding();
        if (b == null) return;
        if(b.clearedShaft) {job.setStage(Stage.WORKING); return;}

        if(b.ladderLocation == null)
        {
            job.setStage(Stage.SEARCHING_LADDER);
            return;
        }

        if(getLocation == null)
        {
            getLocation = new ChunkCoordinates(b.ladderLocation.posX,b.ladderLocation.posY-1,b.ladderLocation.posZ);
        }

        int x = getLocation.posX;
        int y = getLocation.posY;
        int z = getLocation.posZ;
        currentY = getLocation.posY;


            //Needs 39+25 Planks + 4 Torches + 14 fence 5 to create floor-structure
            if (b.startingLevelShaft % 5 == 0 && b.startingLevelShaft != 0)
            {
                if (inventoryContainsMany(b.floorBlock) >= 64 && (inventoryContains(Items.coal)!=-1 || inventoryContainsMany(Blocks.torch) >=4))
                {
                    if (clear < 50)
                    {
                        switch(clear)
                        {
                            case 1:

                                break;
                            case 2:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                break;
                            case 6:
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                            case 7:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 8:
                                if (vektorX == 0)
                                {
                                    x += 1;
                                    z -= 7;

                                }
                                else if (vektorZ == 0)
                                {
                                    z += 1;
                                    x -= 7;
                                }
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 9:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                break;
                            case 13:
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                            case 14:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 15:
                                if (vektorX == 0)
                                {
                                    x += 1;
                                    z -= 7;
                                } else if (vektorZ == 0)
                                {
                                    z += 1;
                                    x -= 7;
                                }
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 16:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                world.setBlock(x, y + 5, z, Blocks.torch);
                                break;
                            case 17:
                            case 18:
                            case 19:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                break;
                            case 20:
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                world.setBlock(x, y + 5, z, Blocks.torch);
                            case 21:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 22:
                                if (vektorX == 0)
                                {
                                    x += 1;
                                    z -= 7;
                                }
                                else if (vektorZ == 0)
                                {
                                    z += 1;
                                    x -= 7;
                                }
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 29:
                                if (vektorX == 0)
                                {
                                    x = x - 4;
                                    z -= 7;
                                }
                                else if (vektorZ == 0)
                                {
                                    z = z - 4;
                                    x -= 7;
                                }
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 30:
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 34:
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                            case 35:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 36:
                                if (vektorX == 0)
                                {
                                    x -= 1;
                                    z -= 7;
                                }
                                else if (vektorZ == 0)
                                {
                                    z -= 1;
                                    x -= 7;
                                }
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 37:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                world.setBlock(x, y + 5, z, Blocks.torch);
                                break;
                            case 38:
                            case 39:
                            case 40:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                break;
                            case 41:
                                world.setBlock(x, y + 4, z, b.fenceBlock);
                                world.setBlock(x, y + 5, z, Blocks.torch);
                            case 42:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                            case 43:
                                if (vektorX == 0)
                                {
                                    x -= 1;
                                    z -= 7;
                                }
                                else if (vektorZ == 0)
                                {
                                    z -= 1;
                                    x -= 7;
                                }
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                                world.setBlock(x, y + 3, z, b.floorBlock);
                                break;
                        }
                        x = x + vektorX;
                        z = z + vektorZ;

                        getLocation.set(x, y, z);
                        clear += 1;
                    }
                    else if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation))
                    {

                        int neededPlanks = 64;
                        int neededTorches = 4;

                        while (neededPlanks > 0)
                        {
                            int slot = inventoryContains(b.floorBlock);
                            int size = worker.getInventory().getStackInSlot(slot).stackSize;
                            worker.getInventory().decrStackSize(slot, size);
                            neededPlanks -= size;
                        }

                        if(inventoryContains(Items.coal)!= -1)
                        {
                            int slot = inventoryContains(Items.coal);
                            worker.getInventory().decrStackSize(slot, 1);
                        }
                        else if(inventoryContainsMany(Blocks.torch) >= 4)
                        {


                            while(neededTorches > 0)
                            {
                                int slot = inventoryContains(Blocks.torch);
                                int size = worker.getInventory().getStackInSlot(slot).stackSize;

                                if (size > 4)
                                {
                                    worker.getInventory().decrStackSize(slot, 4);
                                    neededTorches -= 4;
                                }
                                else
                                {
                                    worker.getInventory().decrStackSize(slot, size);
                                    neededTorches -=size;
                                }


                            }

                        }

                        if (b.levels == null)
                        {
                            b.levels = new ArrayList<Level>();
                        }
                        if (vektorX == 0)
                        {
                            b.levels.add(new Level(b.shaftStart.posX, y + 5, b.shaftStart.posZ + 3,b));

                        }
                        else if (vektorZ == 0)
                        {
                            b.levels.add(new Level(b.shaftStart.posX + 3, y + 5, b.shaftStart.posZ,b));

                        }
                        clear = 1;
                        b.startingLevelShaft++;
                        getLocation.set(b.shaftStart.posX, b.ladderLocation.posY - 1, b.shaftStart.posZ);

                        if (y <= b.getMaxY())
                        {
                            b.clearedShaft = true;
                            job.setStage(Stage.MINING_NODE);
                        }
                        b.markDirty();

                    }

            }
            else
            {
                job.setStage(Stage.INSUFFICIENT_BLOCKS);

                if (inventoryContainsMany(b.floorBlock) >= 64)
                {
                    needItem = Items.coal;
                }
                else
                {
                    needBlock = b.floorBlock;
                }
            }
        }
        //Mining shaft
        else if(inventoryContains(Blocks.dirt)!=-1 && inventoryContains(Blocks.cobblestone)!=-1)
        {
            if (clear >= 50)
            {
                    if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, b.ladderLocation))
                    {
                        b.cobbleLocation.set(b.cobbleLocation.posX, b.ladderLocation.posY - 1, b.cobbleLocation.posZ);
                        b.ladderLocation.set(b.shaftStart.posX, b.ladderLocation.posY - 1, b.shaftStart.posZ);

                        clear = 1;
                        b.startingLevelShaft++;
                        b.markDirty();
                       getLocation.set(b.shaftStart.posX, getLocation.posY-1, b.shaftStart.posZ);
                    }
            }
            else if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker,new ChunkCoordinates(x,y,z)))
                {
                    worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());

                    //if (!world.getBlock(x,y,z).isAir(world,x,y,z))
                    //if (inventoryContains(Blocks.dirt) && inventoryContains(Blocks.cobblestone))
                    hasToMine = world.getBlock(x, y, z);

                    if (hasAllTheTools())
                    {
                        if (!world.getBlock(x, y, z).isAir(world, x, y, z))
                        {
                            if(!doMining(world.getBlock(x, y, z), x, y, z))
                            {
                                return;
                            }
                            if (job.getStage() != Stage.MINING_SHAFT)
                            {
                                return;
                            }

                            logger.info("Mined at " + x + " " + y + " " + z);
                        }
                        if (clear < 50)
                        {
                            //Check if Block after End is empty (Block of Dungeons...)
                            if ((clear - 1) % 7 == 0)
                            {

                                if (world.isAirBlock(x - vektorX, y, z - vektorZ) || !canWalkOn(x - vektorX, y, z - vektorZ))
                                {
                                    setBlockFromInventory(x - vektorX, y, z - vektorZ, Blocks.cobblestone);
                                }
                                isValuable(x - vektorX, y, z - vektorZ);

                            }
                            else if (clear % 7 == 0)
                            {
                                if (world.isAirBlock(x + vektorX, y, z + vektorZ) || !canWalkOn(x + vektorX, y, z + vektorZ))
                                {
                                    setBlockFromInventory(x + vektorX, y, z + vektorZ, Blocks.cobblestone);
                                }
                                isValuable(x + vektorX, y, z + vektorZ);
                            }

                            switch (clear)
                            {
                                case 1:
                                    int meta = world.getBlockMetadata(b.ladderLocation.posX, b.ladderLocation.posY+1, b.ladderLocation.posZ);
                                    setBlockFromInventory(b.cobbleLocation.posX,b.ladderLocation.posY-1,b.cobbleLocation.posZ,Blocks.cobblestone);
                                    world.setBlock(b.ladderLocation.posX, b.ladderLocation.posY-1, b.ladderLocation.posZ, Blocks.ladder, meta, 0x3);
                                    break;
                                case 7:
                                case 14:
                                    if (vektorX == 0)
                                    {
                                        x += 1;
                                        z -= 7;
                                    }
                                    else if (vektorZ == 0)
                                    {
                                        z += 1;
                                        x -= 7;
                                    }
                                    break;
                                case 21:
                                    if (vektorX == 0)
                                    {
                                        x += 1;
                                        z -= 7;
                                    }
                                    else if (vektorZ == 0)
                                    {
                                        z += 1;
                                        x -= 7;
                                    }
                                case 22:
                                case 23:
                                case 24:
                                case 25:
                                case 26:
                                case 27:
                                    if (vektorX == 0)
                                    {
                                        isValuable(x + 1, y, z);

                                        if (world.isAirBlock(x + 1, y, z) || !canWalkOn(x + 1, y, z))
                                        {
                                            setBlockFromInventory(x + 1, y, z, Blocks.cobblestone);
                                        }
                                    }
                                    else if (vektorZ == 0)
                                    {

                                        isValuable(x, y, z + 1);
                                        if (world.isAirBlock(x, y, z + 1) || !canWalkOn(x, y, z + 1))
                                        {
                                            setBlockFromInventory(x, y, z + 1, Blocks.cobblestone);
                                        }
                                    }
                                    break;
                                case 28:
                                    if (vektorX == 0)
                                    {

                                        isValuable(x + 1, y, z);
                                        if (world.isAirBlock(x + 1, y, z) || !canWalkOn(x + 1, y, z))
                                        {
                                            setBlockFromInventory(x + 1, y, z, Blocks.cobblestone);
                                        }
                                    }
                                    else if (vektorZ == 0)
                                    {

                                        isValuable(x, y, z + 1);
                                        if (world.isAirBlock(x, y, z + 1) || !canWalkOn(x, y, z + 1))
                                        {
                                            setBlockFromInventory(x, y, z + 1, Blocks.cobblestone);
                                        }
                                    }
                                    if (vektorX == 0)
                                    {
                                        x = x - 4;
                                        z -= 7;
                                    }
                                    else if (vektorZ == 0)
                                    {
                                        z = z - 4;
                                        x -= 7;
                                    }
                                    break;
                                case 35:
                                    if (clear == 35)
                                    {
                                        if (vektorX == 0)
                                        {
                                            x -= 1;
                                            z -= 7;
                                        }
                                        else if (vektorZ == 0)
                                        {
                                            z -= 1;
                                            x -= 7;
                                        }
                                    }
                                    break;
                                case 42:
                                    if (clear == 42)
                                    {
                                        if (vektorX == 0)
                                        {
                                            x -= 1;
                                            z -= 7;
                                        }
                                        else if (vektorZ == 0)
                                        {
                                            z -= 1;
                                            x -= 7;
                                        }
                                    }
                                case 43:
                                case 44:
                                case 45:
                                case 46:
                                case 47:
                                case 48:
                                case 49:
                                    if (vektorX == 0)
                                    {
                                        isValuable(x - 1, y, z);
                                        if (world.isAirBlock(x - 1, y, z) || !canWalkOn(x - 1, y, z))
                                        {
                                            setBlockFromInventory(x - 1, y, z, Blocks.cobblestone);
                                        }
                                    }
                                    else if (vektorZ == 0)
                                    {
                                        isValuable(x, y, z - 1);
                                        if (world.isAirBlock(x, y, z - 1) || !canWalkOn(x, y, z - 1))
                                        {
                                            setBlockFromInventory(x, y, z - 1, Blocks.cobblestone);
                                        }
                                    }
                                    break;
                            }
                            x = x + vektorX;
                            z = z + vektorZ;

                            if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
                            {
                                setBlockFromInventory(x, y - 1, z, Blocks.dirt);
                            }

                            getLocation.set(x, y, z);
                            clear += 1;
                        }
                    }
            }
        }
        else
            {
                if(inventoryContains(Blocks.cobblestone)==-1)
                {
                    needBlock = Blocks.cobblestone;
                }
                else
                {
                    needBlock = Blocks.dirt;
                }

                job.setStage(Stage.INSUFFICIENT_BLOCKS);
            }
    }

    private int getDelay(Block block,int x, int y, int z)
    {
        return (int)(baseSpeed * worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block, 0) * block.getBlockHardness(world,x,y,z));
    }

    private void setBlockFromInventory(int x, int y, int z, Block block)
    {
        world.setBlock(x, y, z, block);
        int slot = inventoryContains(block);

        if(slot == -1)
        {
            needBlock = block;
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            return;

        }
        worker.getInventory().decrStackSize(slot,1);
    }

    private int inventoryContains(Block block)
    {
        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if(content.equals(block))
                {
                    return slot;
                }
            }
        }

        if(!(inventoryContainsMany(Items.coal)>0 && block == Blocks.torch))
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            needBlock = block;
        }
        return -1;

    }

    private int inventoryContains(Item item)
    {
        if(item == null)
        {
            return -1;
        }

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() != null)
            {
                Item content = stack.getItem();
                if(content.equals(item))
                {
                    return slot;
                }
            }
        }

        if(!(inventoryContainsMany(Blocks.torch)>0 && item == Items.coal))
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            needItem = item;
        }
        return -1;
    }

    private int inventoryContainsMany(Block block)
    {
        int count = 0;

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Block content = ((ItemBlock) stack.getItem()).field_150939_a;
                if(content.equals(block))
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    private int inventoryContainsMany(Item item)
    {
        int count = 0;

        for (int slot = 0; slot < worker.getInventory().getSizeInventory(); slot++)
        {
            ItemStack stack = worker.getInventory().getStackInSlot(slot);

            if (stack != null && stack.getItem() instanceof ItemBlock)
            {
                Item content =  stack.getItem();
                if(content.equals(item))
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    private boolean doMining(Block block, int x, int y, int z)
    {
        BuildingMiner b = (BuildingMiner)(worker.getWorkBuilding());
        if(b == null){return false;}

        ChunkCoordinates bk = new ChunkCoordinates(x,y,z);

        if (InventoryUtils.getOpenSlot(worker.getInventory()) == -1)    //inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                               //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return false;
        }

        /*if(b.activeNode!=null && job.getStage() == Stage.MINING_NODE && (block.isAir(world,x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ()) || !canWalkOn(x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ()))) //-164 58 -225
        {
            b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            b.activeNode.setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Air Node: " + b.active + " x: " + x + " z: " + z + " vectorX: " + b.activeNode.getVectorX() + " vectorZ: " + b.activeNode.getVectorZ());
            b.levels.get(currentLevel).getNodes().remove(b.active);

            return true;
        }*/

        if(job.getStage() == Stage.MINING_NODE && b.shaftStart.posX == x && b.shaftStart.posZ == z)
        {
            b.levels.get(currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            b.activeNode.setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Ladder Node: " + b.active);
            b.levels.get(currentLevel).getNodes().remove(b.active);
            return true;
        }

        if(job.getStage() == Stage.MINING_NODE)
        {
            isValuable(x, y+1, z);
            isValuable(x, y-1, z);
            isValuable(x+b.activeNode.getVectorZ(), y, z+b.activeNode.getVectorX());
            isValuable(x-b.activeNode.getVectorZ(), y, z-b.activeNode.getVectorX());
        }

        if(currentY == 200)
        {
            currentY = y;
        }

        if (block == Blocks.dirt || block == Blocks.gravel || block == Blocks.sand || block == Blocks.clay || block == Blocks.grass)
        {
            holdShovel();
        }
        else
        {
            holdPickAxe();
        }
        hasToMine = block;
        ItemStack tool = worker.getInventory().getHeldItem();
        if (tool == null || !hasAllTheTools())
        {
            job.setStage(Stage.INSUFFICIENT_TOOLS);
            return false;
        }
        else if(!ForgeHooks.canToolHarvestBlock(block,0,tool) && block != Blocks.air && block != Blocks.fence && block !=Blocks.planks && block != Blocks.ladder)
        {
            job.setStage(Stage.NEED_HIGHER_TOOLS);
            hasToMine = block;

            return false;
        }
        else
        {
            avoidCloseLiquid(x,y,z);


            if(!hasDelayed)
            {
                miningBlock = new ChunkCoordinates(x,y,z);
                delay = getDelay(block,x,y,z);
                hasDelayed = true;
                return false;
            }
            miningBlock = null;
            hasDelayed = false;

            tool.getItem().onBlockDestroyed(tool, world, block, x, y, z, worker);//Dangerous
            if (tool.stackSize < 1)//if Tool breaks
            {
                worker.setCurrentItemOrArmor(0, null);
                worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
            }

            world.playSoundEffect(
                    (float) x + 0.5F,
                    (float) y + 0.5F,
                    (float) z + 0.5F,
                    block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 2.0F) / 8.0F, block.stepSound.getPitch() * 0.5F);

            if(job.vein == null)
            {
                if(!isValuable(x, y, z))
                {
                    int fortune = 0;
                    if(tool.isItemEnchanted())
                    {
                        NBTTagList t = tool.getEnchantmentTagList();

                        for(int i=0;i<t.tagCount();i++)
                        {
                            short id = t.getCompoundTagAt(i).getShort("id");
                            if(id == 35)
                            {
                                fortune = t.getCompoundTagAt(i).getShort("lvl");
                            }
                        }
                    }

                    List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, fortune);
                    for (ItemStack item : items)
                    {
                        InventoryUtils.setStack(worker.getInventory(), item);
                    }

                    try
                    {
                        FMLClientHandler.instance().getClient().effectRenderer.addBlockDestroyEffects(x, y, z, block, world.getBlockMetadata(x, y, z));
                    }
                    catch(Exception e)
                    {
                        logger.info("Couldn't add effect");
                    }
                        world.setBlockToAir(x, y, z);
                    blocksMined+=1;
                }
            }
            else
            {
                int fortune = 0;
                if(tool.isItemEnchanted())
                {
                    NBTTagList t = tool.getEnchantmentTagList();

                    for(int i=0;i<t.tagCount();i++)
                    {
                        short id = t.getCompoundTagAt(0).getShort("id");
                        if(id == 35)
                        {
                            fortune = t.getCompoundTagAt(0).getShort("lvl");
                        }
                    }
                }

                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, fortune);
                for (ItemStack item : items)
                {
                    InventoryUtils.setStack(worker.getInventory(), item);
                }
                try
                {
                    FMLClientHandler.instance().getClient().effectRenderer.addBlockDestroyEffects(x, y, z, block, world.getBlockMetadata(x, y, z));
                }
                catch(Exception e)
                {
                    logger.info("Couldn't add effect");
                }
                world.setBlockToAir(x, y, z);
                blocksMined+=1;
            }

            if(job.getStage() == Stage.MINING_VEIN && MathHelper.floor_double(worker.getPosition().xCoord) == x && MathHelper.floor_double(worker.getPosition().yCoord) == y+1 && MathHelper.floor_double(worker.getPosition().zCoord) == z)
            {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if((y < currentY && job.getStage() != Stage.MINING_NODE) && job.getStage() != Stage.MINING_VEIN)
            {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if ((world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z)) && job.getStage() != Stage.MINING_VEIN)
            {
                setBlockFromInventory(x, y - 1, z, Blocks.cobblestone);
            }
        }
        if (!hasAllTheTools())
        {
            job.setStage(Stage.INSUFFICIENT_TOOLS);
        }

        if(blocksMined == 256)
        {
            dumpInventory();

        }

        return true;
    }

    private void findVein(int x, int y, int z)
    {
        job.setStage(Stage.MINING_VEIN);

            for (int x1 = x - 1; x1 <= x + 1; x1++)
            {
                for (int z1 = z - 1; z1 <= z + 1; z1++)
                {
                    for (int y1 = y - 1; y1 <= y + 1; y1++)
                    {
                        if (isValuable(x1, y1, z1))
                        {
                            ChunkCoordinates ore = new ChunkCoordinates(x1, y1, z1);
                            if (!job.vein.contains(ore))
                            {
                                job.vein.add(ore);
                                logger.info("Found close ore");
                            }
                        }
                    }
                }
            }

            if((job.veinId < job.vein.size()))
            {
                ChunkCoordinates v = job.vein.get(job.veinId++);
                logger.info("Check next ore");

                findVein(v.posX, v.posY, v.posZ);
            }
    }

    private int getLastLadder(int x, int y, int z)
    {
        if (world.getBlock(x, y, z).isLadder(world, x, y, z, null))//Parameters unused
        {
            return getLastLadder(x, y - 1, z);
        }
        else
        {
            return y + 1;
        }

    }

    private boolean canWalkOn(int x, int y, int z)
    {
        return world.getBlock(x, y, z).getMaterial().isSolid() && world.getBlock(x,y,z)!=Blocks.web;
    }

    private boolean isValuable(int x, int y, int z)
    {
        Block block = world.getBlock(x,y,z);
        String findOre = block.toString();

        if(job.vein == null && (findOre.contains("ore") || findOre.contains("Ore")))
        {
            job.vein = new ArrayList<ChunkCoordinates>();
            job.vein.add(new ChunkCoordinates(x, y, z));
            logger.info("Found ore");

            findVein(x, y, z);
            logger.info("finished finding ores: " + job.vein.size());
        }

        return findOre.contains("ore") || findOre.contains("Ore");

    }

    public double getBaseSpeed()
    {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed)
    {
        this.baseSpeed = baseSpeed;
    }

}