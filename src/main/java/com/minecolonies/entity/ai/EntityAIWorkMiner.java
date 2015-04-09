package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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

    //TODO be able to change level
    //FIXME Shaft restart bugs
    //TODO Longer Delay!

    private int delay = 0;                   //Must be saved here
    private String NEED_ITEM;

    public List<ChunkCoordinates> localVein;

    private int baseSpeed = 1;

    private int currentY=200;                //Can be saved here
    private int clear = 1;                   //Can be saved here for now

    private Block needBlock = Blocks.dirt;
    private Item needItem;
    private Block hasToMine = Blocks.cobblestone;

    ChunkCoordinates loc;
    private int clearNode=0;

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
        /*if (!Configurations.builderInfiniteResources)
        {
            //requestMaterials();
        }*/

        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    @Override
    public void updateTask()
    {
        BuildingMiner b = (BuildingMiner)(worker.getWorkBuilding());
        if(b == null){return;}

        if(b.shaftStart == null || b.shaftStart.equals(new ChunkCoordinates(0,0,0)) || b.cobbleLocation == null || b.cobbleLocation.equals(new ChunkCoordinates(0,0,0)))
        {
            job.foundLadder = false;
            job.ladderLocation = null;
            findLadder();
            return;
        }

        if((job.ladderLocation == null  && job.getStage() != Stage.SEARCHING_LADDER) || (job.ladderLocation.equals(new ChunkCoordinates(0, 0, 0))  && job.getStage() != Stage.SEARCHING_LADDER))
        {
            job.foundLadder = false;
            job.setStage(Stage.SEARCHING_LADDER);
        }

        if (delay > 0)
        {
            delay--;
        }
        else
        {

            switch (job.getStage())
            {
                case MINING_NODE:
                    if(b.levels!=null)
                    {
                        mineNode(b);
                    }
                    else
                    {
                        createShaft(b.vectorX, b.vectorZ);
                    }
                    break;
                case INSUFFICIENT_TOOLS:
                        //if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                        if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,worker.getWorkBuilding().getLocation()) && ChunkCoordUtils.isClose(worker.getWorkBuilding().getLocation(),worker))
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
                    //if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                    if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,worker.getWorkBuilding().getLocation()) && ChunkCoordUtils.isClose(worker.getWorkBuilding().getLocation(),worker))
                    {
                            if (needBlock == null && needItem != null)
                            {
                                logger.info("Need: " + needItem.getUnlocalizedName());
                            }
                            else if (needItem == null && needBlock != null)
                            {
                                logger.info("Need: " + needBlock.getUnlocalizedName());
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
                    if (!job.foundLadder)
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

                      //  if(ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
                    if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,worker.getWorkBuilding().getLocation()) && ChunkCoordUtils.isClose(worker.getWorkBuilding().getLocation(),worker))
                    {
                            logger.info("Need better Tools");
                            delay = 30;

                            if (hasAllTheTools()) {
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
        if(b.levels.size()<b.currentLevel+1)
        {
            b.activeNode = null;
            job.setStage(Stage.MINING_SHAFT);
            return;
        }

        Level level = b.levels.get(b.currentLevel);
        int depth = level.getDepth();

        if(level.getNodes().size() == 0)
        {
            b.currentLevel++;
            return;
        }

        //FIXME Using Wrong Tool while Mining Node
        //FIXME can't climb ladder when moving ladder to side
        //TODO Last: Use Wood and Coal
        if(b.activeNode == null || b.activeNode.getStatus() == Node.Status.COMPLETED)
        {

            int rand1 = (int)(Math.random())*3;
            int randomNum;

            if(b.levels.get(b.currentLevel).getNodes() == null)
            {
                if(b.levels.size()<b.currentLevel+1)
                {
                    b.activeNode = null;
                    job.setStage(Stage.MINING_SHAFT);
                }
                else
                {
                    b.currentLevel +=1;
                }
            }

            if(rand1 != 1)
            {
                randomNum = b.active;

                if(b.levels.get(b.currentLevel).getNodes().size()<b.active)
                {
                    return;
                }
            }
            else
            {
                 randomNum = (int) (Math.random() * b.levels.get(b.currentLevel).getNodes().size());
            }

            Node node = b.levels.get(b.currentLevel).getNodes().get(randomNum);

            if(node.getStatus() == Node.Status.AVAILABLE)
            {
                int x1 = b.shaftStart.posX + b.getMaxX();
                int x2 = b.shaftStart.posX - b.getMaxX();
                int z1 = b.shaftStart.posZ + b.getMaxZ();
                int z2 = b.shaftStart.posZ - b.getMaxZ();

                if(node.getID().getX() > x1 || node.getID().getY() > z1  ||  node.getID().getX() < x2 || node.getID().getY() < z2)
                {
                    b.levels.get(b.currentLevel).getNodes().remove(randomNum);
                    return;
                }

                logger.info("Starting Node: " + randomNum);

                loc  = new ChunkCoordinates(node.getID().getX(), depth, node.getID().getY());
                b.activeNode = node;
                b.active = randomNum;
                b.activeNode.setStatus(Node.Status.IN_PROGRESS);
                clearNode = 0;
                b.startingLevelNode = 0;
                b.markDirty();
            }
        }
        else if(b.activeNode.getStatus() == Node.Status.IN_PROGRESS && loc !=null)
        {
               //if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, new ChunkCoordinates(loc.posX, loc.posY - 1, loc.posZ)))
            if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,new ChunkCoordinates(loc.posX, loc.posY - 1, loc.posZ)) && ChunkCoordUtils.isClose(new ChunkCoordinates(loc.posX, loc.posY - 1, loc.posZ),worker))
            {
                    Block block;
                    int uVX = 0;
                    int uVZ = 0;

                    if (b.startingLevelNode == 5)
                    {
                        b.levels.get(b.currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
                        b.activeNode.setStatus(Node.Status.COMPLETED);

                        b.levels.get(b.currentLevel).addNewNode(b.activeNode.getID().getX() + 5 * b.activeNode.getVectorX(), b.activeNode.getID().getY() + 5 * b.activeNode.getVectorZ(), b.activeNode.getVectorX(), b.activeNode.getVectorZ());

                        if (b.activeNode.getVectorX() == 0)
                        {
                            b.levels.get(b.currentLevel).addNewNode(b.activeNode.getID().getX() + 2, b.activeNode.getID().getY() + 4 * b.activeNode.getVectorZ(), unsignVector(b.activeNode.getVectorZ()), unsignVector(b.activeNode.getVectorX()));
                            b.levels.get(b.currentLevel).addNewNode(b.activeNode.getID().getX() - 2, b.activeNode.getID().getY() + 4 * b.activeNode.getVectorZ(), -unsignVector(b.activeNode.getVectorZ()), -unsignVector(b.activeNode.getVectorX()));
                        }
                        else
                        {
                            b.levels.get(b.currentLevel).addNewNode(b.activeNode.getID().getX() + 4 * b.activeNode.getVectorX(), b.activeNode.getID().getY() + 2, unsignVector(b.activeNode.getVectorZ()), unsignVector(b.activeNode.getVectorX()));
                            b.levels.get(b.currentLevel).addNewNode(b.activeNode.getID().getX() + 4 * b.activeNode.getVectorX(), b.activeNode.getID().getY() - 2, -unsignVector(b.activeNode.getVectorZ()), -unsignVector(b.activeNode.getVectorX()));
                        }
                        logger.info("Finished Node: " + b.active);
                        b.levels.get(b.currentLevel).getNodes().remove(b.active);

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
                                ChunkCoordinates mineThere = new ChunkCoordinates(loc.posX, loc.posY + 1, loc.posZ);
                                checkAbove(loc.posX, loc.posY + 2, loc.posZ);
                                block = ChunkCoordUtils.getBlock(world, mineThere);
                                doMining(block, loc.posX, loc.posY + 1, loc.posZ);
                                clearNode += 1;
                            case 1:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ));
                                checkAbove(loc.posX - uVX, loc.posY + 2, loc.posZ - uVZ);
                                doMining(block, loc.posX - uVX, loc.posY + 1, loc.posZ - uVZ);
                                clearNode += 1;
                                break;
                            case 2:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ));
                                checkAbove(loc.posX + uVX, loc.posY + 2, loc.posZ + uVZ);
                                doMining(block, loc.posX + uVX, loc.posY + 1, loc.posZ + uVZ);
                                clearNode += 1;
                                break;
                            case 3:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX + uVX, loc.posY, loc.posZ + uVZ));
                                doMining(block, loc.posX + uVX, loc.posY, loc.posZ + uVZ);
                                clearNode += 1;
                                break;
                            case 4:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX, loc.posY, loc.posZ));
                                doMining(block, loc.posX, loc.posY, loc.posZ);
                                clearNode += 1;
                                break;
                            case 5:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX - uVX, loc.posY, loc.posZ - uVZ));
                                doMining(block, loc.posX - uVX, loc.posY, loc.posZ - uVZ);
                                clearNode += 1;
                                break;
                            case 6:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ));
                                doMining(block, loc.posX - uVX, loc.posY - 1, loc.posZ - uVZ);
                                clearNode += 1;
                                break;
                            case 7:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX, loc.posY - 1, loc.posZ));
                                doMining(block, loc.posX, loc.posY - 1, loc.posZ);
                                clearNode += 1;
                                break;
                            case 8:
                                block = ChunkCoordUtils.getBlock(world, new ChunkCoordinates(loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ));
                                doMining(block, loc.posX + uVX, loc.posY - 1, loc.posZ + uVZ);
                                clearNode += 1;
                                break;
                            case 9:

                                if (b.startingLevelNode == 2)
                                {
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
                                    }
                                    else if (b.activeNode.getVectorZ() > 0)
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

    private void checkAbove(int x,int y,int z)
    {
        ChunkCoordinates above = new ChunkCoordinates(x,y, z);
        Block blockAbove = ChunkCoordUtils.getBlock(world,above);
        if(blockAbove == Blocks.sand || blockAbove == Blocks.gravel || !canWalkOn(x,y,z))
        {
            ChunkCoordUtils.setBlock(world,above,Blocks.cobblestone);
            int slot = inventoryContains(Blocks.cobblestone);
            worker.getInventory().decrStackSize(slot, 1);
        }

    }

    private boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getItem().getToolClasses(null /* not used */).contains("pickaxe") || stack.getItem().getToolClasses(null /* not used */).contains("shovel"));
    }

    private void isInHut(Block block)
    {
        if(worker.getWorkBuilding().getTileEntity()==null)
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            needBlock = block;
            return;
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
                    }
            }
        }
    }

    private void isInHut(Item item)
    {
        if(worker.getWorkBuilding().getTileEntity()==null)
        {
            job.setStage(Stage.INSUFFICIENT_BLOCKS);
            needItem = item;
            return;
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
                }
            }
        }
    }

    private void dumpInventory()
    {

       //if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        if(ChunkCoordUtils.tryMoveLivingToXYZ(worker, worker.getWorkBuilding().getLocation()) && ChunkCoordUtils.isClose(worker.getWorkBuilding().getLocation(),worker))
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
            }
        }

    private void mineVein()
    {
        //FIXME If ore under worker, already fill with cobble!
        BuildingMiner b = (BuildingMiner)worker.getWorkBuilding();
        if (b == null) return;

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
            doMining(block, nextLoc.posX, nextLoc.posY, nextLoc.posZ);
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

    private void avoidCloseLiquid(int x, int y, int z)
    {
        for (int x1 = x - 3; x1 <= x + 3; x1++)
        {
            for (int z1 = z - 3; z1 <= z + 3; z1++)
            {
                for (int y1 = y ; y1 <= y + 2; y1++)
                {
                    Block block = world.getBlock(x1,y1,z1);
                    if (block == Blocks.lava || block == Blocks.flowing_lava || block == Blocks.water || block == Blocks.flowing_water)//Add Mod Liquids
                    {
                        setBlockFromInventory(x, y-1, z, Blocks.dirt);
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

        if(b.getLocation == null)
        {
            b.getLocation = job.ladderLocation;
        }

        int posX = worker.getWorkBuilding().getLocation().posX;
        int posY = worker.getWorkBuilding().getLocation().posY + 2;
        int posZ = worker.getWorkBuilding().getLocation().posZ;
        int posYWorker = posY;

        for (int x = posX - 10; x < posX + 10; x++)
        {
            for (int z = posZ - 10; z < posZ + 10; z++)
            {
                for (int y = posY - 10; y < posY; y++)
                {
                    if (job.foundLadder)
                    {
                        job.setStage(Stage.MINING_SHAFT);
                        return;
                    }
                    else
                    if (world.getBlock(x, y, z).equals(Blocks.ladder))//Parameters unused
                    {

                            int lastY = getLastLadder(x, y, z);
                            job.ladderLocation = new ChunkCoordinates(x, lastY, z);
                            posYWorker = lastY;
                            logger.info("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                            delay = 10;


                        //if (/*ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation) &&*/  ChunkCoordUtils.tryMoveLivingToXYZ(worker, job.ladderLocation))
                        if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,job.ladderLocation) && ChunkCoordUtils.isClose(job.ladderLocation,worker))
                        {
                            //if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation))
                            {
                                b.cobbleLocation = new ChunkCoordinates(x, posYWorker, z);

                                //Cobble on x+1 x-1 z+1 or z-1 to the ladder
                                if (world.getBlock(job.ladderLocation.posX - 1, job.ladderLocation.posY, job.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                                {
                                    b.cobbleLocation = new ChunkCoordinates(x - 1, posYWorker, z);
                                    b.vectorX = 1;
                                    b.vectorZ = 0;
                                    logger.info("Found cobble - West");
                                    //West

                                }
                                else if (world.getBlock(job.ladderLocation.posX + 1, job.ladderLocation.posY, job.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                                {
                                    b.cobbleLocation = new ChunkCoordinates(x + 1, posYWorker, z);
                                    b.vectorX = -1;
                                    b.vectorZ = 0;
                                    logger.info("Found cobble - East");
                                    //East

                                }
                                else if (world.getBlock(job.ladderLocation.posX, job.ladderLocation.posY, job.ladderLocation.posZ - 1).equals(Blocks.cobblestone))//Parameters unused
                                {
                                    b.cobbleLocation = new ChunkCoordinates(x, posYWorker, z - 1);
                                    b.vectorZ = 1;
                                    b.vectorX = 0;
                                    logger.info("Found cobble - South");
                                    //South

                                }
                                else if (world.getBlock(job.ladderLocation.posX, job.ladderLocation.posY, job.ladderLocation.posZ + 1).equals(Blocks.cobblestone))//Parameters unused
                                {
                                    b.cobbleLocation = new ChunkCoordinates(x, posYWorker, z + 1);
                                    b.vectorZ = -1;
                                    b.vectorX = 0;
                                    logger.info("Found cobble - North");
                                    //North
                                }
                                //world.setBlockToAir(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                                b.getLocation = new ChunkCoordinates(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);
                                b.shaftStart = new ChunkCoordinates(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);
                                job.foundLadder = true;
                                hasAllTheTools();
                                job.setStage(Stage.WORKING);
                                b.markDirty();
                            }
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

        if(job.ladderLocation == null)
        {
            job.setStage(Stage.SEARCHING_LADDER);
            return;
        }

        if(b.getLocation == null)
        {
            b.getLocation = job.ladderLocation;
        }

        int x = b.getLocation.posX;
        int y = b.getLocation.posY;
        int z = b.getLocation.posZ;
        currentY = b.getLocation.posY;


            //Needs 39+25 Planks + 4 Torches + 14 fence 5
            if (b.startingLevelShaft % 5 == 0 && b.startingLevelShaft != 0)
            {
                if (inventoryContainsMany(b.floorBlock) >= 64 && inventoryContains(Items.coal)!=-1)
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

                        b.getLocation.set(x, y, z);
                        clear += 1;
                    }
                    //if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation))
                    else if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,job.ladderLocation) && ChunkCoordUtils.isClose(job.ladderLocation,worker))
                    {

                            int neededPlanks = 64;

                            while (neededPlanks > 0)
                            {
                                int slot = inventoryContains(b.floorBlock);
                                int size = worker.getInventory().getStackInSlot(slot).stackSize;
                                worker.getInventory().decrStackSize(slot, size);
                                neededPlanks -= size;
                            }

                            int slot = inventoryContains(Items.coal);
                            worker.getInventory().decrStackSize(slot, 1);
                            //Save Node
                            //(x-4, y+1, z) (x, y+1, Z+4) and (x+4, y+1, z) or (x,y+1,z-1) in case of rotation -> check ladder

                            if (b.levels == null)
                            {
                                b.levels = new ArrayList<Level>();
                            }
                            if (vektorX == 0)
                            {
                                b.levels.add(new Level(b.shaftStart.posX, y + 5, b.shaftStart.posZ + 3));

                            }
                            else if (vektorZ == 0)
                            {
                                b.levels.add(new Level(b.shaftStart.posX + 3, y + 5, b.shaftStart.posZ));

                            }
                            clear = 1;
                            b.startingLevelShaft++;
                            b.getLocation.set(b.shaftStart.posX, job.ladderLocation.posY-1, b.shaftStart.posZ);

                            if (y <= b.getMaxY())
                            {
                                b.clearedShaft = true;
                                job.setStage(Stage.MINING_NODE);

                                //If level = +/- long ago, build on y or -1
                            }
                        b.markDirty();

                        }

                }
                else
                {
                    job.setStage(Stage.INSUFFICIENT_BLOCKS);

                    if(inventoryContainsMany(b.floorBlock)>= 64)
                    {
                        needItem= Items.coal;
                    }
                    else
                    {
                        needBlock = b.floorBlock;
                    }
                }

            }
        else if(inventoryContains(Blocks.dirt)!=-1 && inventoryContains(Blocks.cobblestone)!=-1)
        {
            if (clear >= 50)
            {
                //if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation))
                    if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,job.ladderLocation) && ChunkCoordUtils.isClose(job.ladderLocation,worker))
                    {
                        b.cobbleLocation.set(b.cobbleLocation.posX, job.ladderLocation.posY - 1, b.cobbleLocation.posZ);
                        job.ladderLocation.set(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);

                        clear = 1;
                        b.startingLevelShaft++;
                        b.markDirty();
                        b.getLocation.set(b.shaftStart.posX, job.ladderLocation.posY - 1, b.shaftStart.posZ);
                    }
            }
            //else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, new ChunkCoordinates(x, y, z)))
              else if(ChunkCoordUtils.tryMoveLivingToXYZ(worker,new ChunkCoordinates(x,y,z)) && ChunkCoordUtils.isClose(new ChunkCoordinates(x,y,z),worker))
                {
                    worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());

                    //if (!world.getBlock(x,y,z).isAir(world,x,y,z))
                    //if (inventoryContains(Blocks.dirt) && inventoryContains(Blocks.cobblestone))
                    hasToMine = world.getBlock(x, y, z);

                    if (hasAllTheTools())
                    {
                        if (!world.getBlock(x, y, z).isAir(world, x, y, z))
                        {
                            doMining(world.getBlock(x, y, z), x, y, z);
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
                                    int meta = world.getBlockMetadata(job.ladderLocation.posX, job.ladderLocation.posY+1, job.ladderLocation.posZ);
                                    setBlockFromInventory(b.cobbleLocation.posX,job.ladderLocation.posY-1,b.cobbleLocation.posZ,Blocks.cobblestone);
                                    world.setBlock(job.ladderLocation.posX, job.ladderLocation.posY-1, job.ladderLocation.posZ, Blocks.ladder, meta, 0x3);
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

                            b.getLocation.set(x, y, z);
                            clear += 1;
                        }
                    }

            }
        }
    }

    private int getDelay(Block block,int x, int y, int z)
    {

        int dd = baseSpeed  * (int)(worker.getHeldItem().getItem().getDigSpeed(worker.getHeldItem(), block, 0) * block.getBlockHardness(world,x,y,z));
        return dd;

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

        job.setStage(Stage.INSUFFICIENT_BLOCKS);
        needBlock = block;
        return -1;

    }

    private int inventoryContains(Item item)
    {
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

        job.setStage(Stage.INSUFFICIENT_BLOCKS);
        needItem = item;
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


    private void doMining(Block block, int x, int y, int z)
    {
        BuildingMiner b = (BuildingMiner)(worker.getWorkBuilding());
        if(b == null){return;};

        if (InventoryUtils.getOpenSlot(worker.getInventory()) == -1)    //inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                               //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);

            if(job.getStage() == Stage.MINING_NODE)
            {
                clearNode-=1;
            }
            return;
        }

        if(b.activeNode!=null && job.getStage() == Stage.MINING_NODE && (block.isAir(world,x,y,z)
                || !canWalkOn(x,y,z)
                || block.isAir(world,x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ())
                || !canWalkOn(x+b.activeNode.getVectorX(),y,z+b.activeNode.getVectorZ())))
        {
            b.levels.get(b.currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            b.activeNode.setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Air Node: " + b.active + " x: " + x + " z: " + z + " vectorX: " + b.activeNode.getVectorX() + " vectorZ: " + b.activeNode                  .getVectorZ());
            b.levels.get(b.currentLevel).getNodes().remove(b.active);

            return;
        }

        if(job.getStage() == Stage.MINING_NODE && b.shaftStart.posX == x && b.shaftStart.posZ == z)
        {
            b.levels.get(b.currentLevel).getNodes().get(b.active).setStatus(Node.Status.COMPLETED);
            b.activeNode.setStatus(Node.Status.COMPLETED);
            logger.info("Finished because of Ladder Node: " + b.active);
            b.levels.get(b.currentLevel).getNodes().remove(b.active);

            return;

        }

        if(job.getStage() == Stage.MINING_NODE)
        {
            isValuable(x, y+1, z);
            isValuable(x, y-1, z);
            isValuable(x+b.activeNode.getVectorZ(), y+1, z+b.activeNode.getVectorX());
            isValuable(x-b.activeNode.getVectorZ(), y+1, z-b.activeNode.getVectorX());
        }

        ChunkCoordinates bk = new ChunkCoordinates(x,y,z);

        if(currentY == 200)
        {
            currentY = y;
        }

        if (block == Blocks.dirt || block == Blocks.gravel || block == Blocks.sand || block == Blocks.clay || block == Blocks.grass)
        {
            holdShovel();
        }
        else if(block == Blocks.planks || block == Blocks.fence)
        {
            worker.setHeldItem(0);
        }
        else
        {
            holdPickAxe();
        }
        hasToMine = block;
        //Damage Tools
        ItemStack Tool = worker.getInventory().getHeldItem();
        if (Tool == null || !hasAllTheTools())
        {
            job.setStage(Stage.INSUFFICIENT_TOOLS);

            if(job.getStage() == Stage.MINING_NODE)
            {
                clearNode-=1;
            }

        }
        else if(!ForgeHooks.canToolHarvestBlock(block,0,Tool) && block != Blocks.air && block != Blocks.fence && block !=Blocks.planks && block != Blocks.ladder)
        {
            job.setStage(Stage.NEED_HIGHER_TOOLS);
            hasToMine = block;

            if(job.getStage() == Stage.MINING_NODE)
            {
                clearNode-=1;
            }
        }
        else
        {
            delay = getDelay(block,x,y,z);
            while(delay > 0)
            {
                delay --;
            }

            Tool.getItem().onBlockDestroyed(Tool, world, block, x, y, z, worker);//Dangerous
            if (Tool.stackSize < 1)//if Tool breaks
            {
                worker.setCurrentItemOrArmor(0, null);
                worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
                //TODO particles
            }

            world.playSoundEffect(
                    (float) x + 0.5F,
                    (float) y + 0.5F,
                    (float) z + 0.5F,
                    block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 2.0F) / 8.0F, block.stepSound.getPitch() * 0.5F);
            //TODO particles
            worker.swingItem();
            //Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(log.posX, log.posY, log.posZ, block, ChunkCoordUtils.getBlockMetadata(world, log));

            if(job.vein == null)
            {
                if(!isValuable(x, y, z))
                {
                    List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, 0);//0 is fortune level, it doesn't matter
                    for (ItemStack item : items)
                    {
                        InventoryUtils.setStack(worker.getInventory(), item);
                    }
                    world.setBlockToAir(x, y, z);

                }
            }
            else
            {
                List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, 0);//0 is fortune level, it doesn't matter
                for (ItemStack item : items)
                {
                    InventoryUtils.setStack(worker.getInventory(), item);
                }
                world.setBlockToAir(x, y, z);
            }

            if((y < currentY && job.getStage() != Stage.MINING_NODE) && job.getStage() != Stage.MINING_VEIN)
            {
                setBlockFromInventory(x, y, z, Blocks.cobblestone);
            }

            if ((world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z)) && job.getStage() != Stage.MINING_VEIN)
            {
                setBlockFromInventory(x, y-1, z, Blocks.cobblestone);
            }
        }
        if (!hasAllTheTools())
        {
            job.setStage(Stage.INSUFFICIENT_TOOLS);
        }
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

            if (job.getStage() == Stage.MINING_NODE)
            {
                //clearNode -= 1;
            }
            else if (job.getStage() == Stage.MINING_SHAFT)
            {
                //clear -= 1;
            }

            findVein(x, y, z);
            logger.info("finished finding ores: " + job.vein.size());
        }

        return findOre.contains("ore") || findOre.contains("Ore");

    }

    /*
    public int getBaseSpeed()
    {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed)
    {
        this.baseSpeed = baseSpeed;
    }
    */

}