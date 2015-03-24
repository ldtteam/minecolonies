package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
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

    private int clear = 1;
    private int vektorx = 1;
    private int vektorz = 1;
    private List<ChunkCoordinates> Veine;
    private int VeineId=0;
    private int delay = 0;
    private int currentY=200;
    private boolean SufficientTools = false;
    private boolean SufficientItems = true;
    private Block needBlock = Blocks.dirt;
    private Boolean InventoryFull = false;
    //TODO Gather ores himself
//TODO Copy Code from Job to building and cast the getWorkBuilding to buildingMiner
/*
BuildingMiner b = citizen.getWorkBuilding(BuildingMiner.class);
if (b == null) return;
 */
    //Node Position if Node is close to x+5 or z+5 to other Node delete them Both

    private static final int MAXIMUM_LEVEL = 4;

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
        if (!Configurations.builderInfiniteResources)
        {
            //requestMaterials();
        }

        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    @Override
    public void updateTask()
    {
        //TODO Check if Ladder is still there after time
        if (delay > 0)
        {
            delay--;
        }
        else if (!SufficientTools)
        {
            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))//Go Home
            {
                logger.info("Need tools");
                delay = 20;
                if (hasAllTheTools())
                {
                    SufficientTools = true;
                } // Add Torches
            }
        }
        else if (!SufficientItems)
        {
            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))//Go Home
            {
                logger.info("Need" + needBlock.toString());
                delay = 20;
                if (inventoryContains(needBlock)!=-1)
                {
                    SufficientItems = true;
                } // Add Torches
            }
        }
        else if(InventoryFull)
        {
            dumpInventory();
        }
        else if (!job.foundLadder)
        {
            findLadder();
        }
        else if (Veine != null)
        {
            mineVeine();

        }
        else if (!job.cleared)//has ladder, tools and not clear
        {
            createShaft(vektorx, vektorz);
        }

        //go to Chest

        /*
        Rough outline:
            Data structures:
                Nodes are 5x5x4 (3 tall + a ceiling)
                connections are 3x5x3 tunnels
            Connections should be automatically completed when moving from node to node

            max Level depth depends on current hut startingLevel
                example:
                    1: y=44
                    2: y=28
                    3: y=10
                Personally I think our lowest Level should be 4 or 5, whatever one you can't run into bedrock on

            If the miner has a node, then he should create the connection, then mine the node

            else findNewNode

            That's basically it...
            Also note we need to check the tool durability and for torches,
                wood for building the tunnel structure (exact plan to be determined)

            You also may want to create another node status before AVAILABLE for when the connection isn't completed.
                Maybe even two status, one for can_connect_too then connection_in_progress
         */
    }
    private boolean isStackTool(ItemStack stack)
    {
        return stack != null && (stack.getItem().getToolClasses(null /* not used */).contains("pickaxe") || stack.getItem().getToolClasses(null /* not used */).contains("shovel"));
    }

    private void dumpInventory()
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            for (int i = 0; i < worker.getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = worker.getInventory().getStackInSlot(i);
                if (stack != null && !isStackTool(stack))
                {

                        ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);//TODO tile entity null
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
            InventoryFull = false;
        }
    }

    private void mineVeine()
    {
        worker.getWorkBuilding();
        if(Veine.size() == 0)
        {
            Veine = null;
            VeineId = 0;
        }
        else
        {
            List<ChunkCoordinates> localVeine = new ArrayList<ChunkCoordinates>();
            localVeine = Veine;
            ChunkCoordinates nextLoc = Veine.get(0);
            Block block = ChunkCoordUtils.getBlock(world, nextLoc);
            int x = nextLoc.posX;
            int y = nextLoc.posY;
            int z = nextLoc.posZ;
            doMiningAnimation(block, nextLoc.posX, nextLoc.posY, nextLoc.posZ);
            Veine.remove(0);

            if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
            {
                world.setBlock(x, y - 1, z, Blocks.dirt);
                int slot = inventoryContains(Blocks.dirt);
                worker.getInventory().decrStackSize(slot, 1);



            }
            avoidCloseLiquid(x,y,z);

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
                        world.setBlock(x, y - 1, z, Blocks.dirt);
                        int slot = inventoryContains(Blocks.dirt);
                        worker.getInventory().decrStackSize(slot, 1);
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

        return hasSpade > 0 || hasSpadeInHand && hasPickAxeInHand || hasPickAxe > 0;
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

    // public void DigForNode(){};
    // public void createNode(){};
    // public void createShaft(){};
    private void findLadder()
    {
        int posX = worker.getWorkBuilding().getLocation().posX;
        int posY = worker.getWorkBuilding().getLocation().posY + 2;
        int posZ = worker.getWorkBuilding().getLocation().posZ;
        int posYWorker = posY;

        for (int x = posX - 10; x < posX + 15; x++)
        {
            for (int z = posZ - 10; z < posZ + 15; z++)
            {
                for (int y = posY - 10; y < posY; y++)
                {
                    if (job.foundLadder)
                        return;

                    if (world.getBlock(x, y, z).equals(Blocks.ladder))//Parameters unused
                    {
                        if (job.ladderLocation == null)
                        {
                            int lastY = getLastLadder(x, y, z);
                            job.ladderLocation = new ChunkCoordinates(x, lastY, z);
                            posYWorker = lastY;
                            logger.info("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                        }

                        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation))
                        {
                            job.cobbleLocation = new ChunkCoordinates(x, posYWorker, z);

                            //Cobble on x+1 x-1 z+1 or z-1 to the ladder
                            if (world.getBlock(job.ladderLocation.posX - 1, job.ladderLocation.posY, job.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                job.cobbleLocation = new ChunkCoordinates(x - 1, posYWorker, z);
                                vektorx = 1;
                                vektorz = 0;
                                logger.info("Found cobble - West");
                                //West

                            }
                            else if (world.getBlock(job.ladderLocation.posX + 1, job.ladderLocation.posY, job.ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                job.cobbleLocation = new ChunkCoordinates(x + 1, posYWorker, z);
                                vektorx = -1;
                                vektorz = 0;
                                logger.info("Found cobble - East");
                                //East

                            }
                            else if (world.getBlock(job.ladderLocation.posX, job.ladderLocation.posY, job.ladderLocation.posZ - 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                job.cobbleLocation = new ChunkCoordinates(x, posYWorker, z - 1);
                                vektorz = 1;
                                vektorx = 0;
                                logger.info("Found cobble - South");
                                //South

                            }
                            else if (world.getBlock(job.ladderLocation.posX, job.ladderLocation.posY, job.ladderLocation.posZ + 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                job.cobbleLocation = new ChunkCoordinates(x, posYWorker, z + 1);
                                vektorz = -1;
                                vektorx = 0;
                                logger.info("Found cobble - North");
                                //North
                            }
                            //world.setBlockToAir(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                            job.getLocation = new ChunkCoordinates(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);
                            job.foundLadder = true;
                        }
                    }
                }
            }
        }
    }

    private void createShaft(int vektorX, int vektorZ)
    {
        //TODO first found ore in wall not replaced by cobblestone

        int x = job.getLocation.posX;
        int y = job.getLocation.posY;
        int z = job.getLocation.posZ;
        currentY = job.getLocation.posY;
        if (InventoryUtils.getOpenSlot(worker.getInventory()) == -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            InventoryFull = true;
            return;
        }

        if(inventoryContains(Blocks.dirt)!=-1 && inventoryContains(Blocks.cobblestone)!=-1)
        {

            if (job.startingLevel % 5 == 0 && job.startingLevel != 0)
            {
                if (inventoryContains(Blocks.planks)!=-1)
                {
                    if (clear < 50)
                    {
                        if (clear == 1)
                        {
                            // Do nothing
                        }
                        else if (clear == 2)
                        {
                            world.setBlock(x, y + 3, z, job.floorBlock);
                            world.setBlock(x, y + 4, z, job.fenceBlock);
                        }
                        else if (clear <= 5)
                        {
                            // Do nothing
                        }
                        else if (clear <= 7)
                        {
                            if (clear == 6)
                            {
                                world.setBlock(x, y + 4, z, job.fenceBlock);
                            }

                            world.setBlock(x, y + 3, z, job.floorBlock);
                        }
                        else if (clear <= 14)
                        {
                            if (clear == 8)
                            {
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
                                world.setBlock(x, y + 3, z, job.floorBlock);

                            }
                            else if (clear == 9)
                            {
                                world.setBlock(x, y + 3, z, job.floorBlock);
                                world.setBlock(x, y + 4, z, job.fenceBlock);
                            }
                            else if (clear > 12)
                            {
                                if (clear == 13)
                                {
                                    world.setBlock(x, y + 4, z, job.fenceBlock);
                                }

                                world.setBlock(x, y + 3, z, job.floorBlock);
                            }

                        }
                        else if (clear <= 21)
                        {
                            if (clear == 15)
                            {
                                if (vektorX == 0)
                                {
                                    x += 1;
                                    z -= 7;
                                } else if (vektorZ == 0) {
                                    z += 1;
                                    x -= 7;
                                }

                            }
                            if (clear > 15 && clear < 21)
                            {
                                world.setBlock(x, y + 4, z, job.fenceBlock);

                                if (clear == 16 || clear == 20)
                                {
                                    world.setBlock(x, y + 5, z, Blocks.torch);
                                }
                            }

                            world.setBlock(x, y + 3, z, job.floorBlock);

                        }
                        else if (clear <= 28)
                        {
                            if (clear == 22)
                            {
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
                            }
                            world.setBlock(x, y + 3, z, job.floorBlock);

                        }
                        else if (clear <= 35)
                        {
                            if (clear == 29)
                            {
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
                                world.setBlock(x, y + 3, z, job.floorBlock);

                            }
                            else if (clear == 30) {

                                world.setBlock(x, y + 3, z, job.floorBlock);
                                world.setBlock(x, y + 4, z, job.fenceBlock);
                            }
                            else if (clear > 33)
                            {
                                if (clear == 34)
                                {
                                    world.setBlock(x, y + 4, z, job.fenceBlock);
                                }

                                world.setBlock(x, y + 3, z, job.floorBlock);
                            }
                        } else if (clear <= 42)
                        {
                            if (clear == 36)
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
                            if (clear > 36 && clear < 42)
                            {
                                world.setBlock(x, y + 4, z, job.fenceBlock);
                                if (clear == 37 || clear == 41)
                                {
                                    world.setBlock(x, y + 5, z, Blocks.torch);
                                }
                            }

                            world.setBlock(x, y + 3, z, job.floorBlock);
                        }
                        else if (clear <= 49)
                        {
                            if (clear == 43)
                            {
                                if (vektorX == 0)
                                {
                                    x -= 1;
                                    z -= 7;
                                } else if (vektorZ == 0) {
                                    z -= 1;
                                    x -= 7;
                                }
                            }
                            world.setBlock(x, y + 3, z, job.floorBlock);
                        }

                        x = x + vektorX;
                        z = z + vektorZ;

                        job.getLocation.set(x, y, z);
                        clear += 1;
                    }
                    else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation))
                    {
                        //Save Node
                        //(x-4, y+1, z) (x, y+1, Z+4) and (x+4, y+1, z)
                        job.levels = new ArrayList<Level>();
                        job.levels.add(new Level(y + 3));//FIXME nothing is done with this
                        clear = 1;
                        job.startingLevel++;
                        job.getLocation.set(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);
                    }
                }
            }
            else if (clear >= 50)
            {
                if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.ladderLocation))
                {
                    int meta = world.getBlockMetadata(job.ladderLocation.posX, job.ladderLocation.posY, job.ladderLocation.posZ);
                    job.cobbleLocation.set(job.cobbleLocation.posX, job.ladderLocation.posY - 1, job.cobbleLocation.posZ);
                    job.ladderLocation.set(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);
                    world.setBlock(job.cobbleLocation.posX, job.cobbleLocation.posY, job.cobbleLocation.posZ, Blocks.cobblestone);
                    world.setBlock(job.ladderLocation.posX, job.ladderLocation.posY, job.ladderLocation.posZ, Blocks.ladder, meta, 0x3);

                    int slot = inventoryContains(Blocks.cobblestone);
                    worker.getInventory().decrStackSize(slot,1);

                    if (y <= MAXIMUM_LEVEL)
                    {
                        job.cleared = true;
                        //If level = +/- long ago, build on y or -1
                    }
                    clear = 1;
                    job.startingLevel++;
                    job.getLocation.set(job.ladderLocation.posX, job.ladderLocation.posY - 1, job.ladderLocation.posZ);
                }
            }
            else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, new ChunkCoordinates(x, y, z)))
            {

                worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());

                //if (!world.getBlock(x,y,z).isAir(world,x,y,z))
                //if (inventoryContains(Blocks.dirt) && inventoryContains(Blocks.cobblestone))
                {
                    if (!world.getBlock(x, y, z).isAir(world, x, y, z))
                    {

                        doMiningAnimation(world.getBlock(x, y, z), x, y, z);


                        logger.info("Mined at " + x + " " + y + " " + z);
                    }
                    if (clear < 50)
                    {
                        //Check if Block after End is empty (Block of Dungeons...)
                        if ((clear - 1) % 7 == 0)
                        {

                            if (world.isAirBlock(x - vektorX, y, z - vektorZ) || !canWalkOn(x - vektorX, y, z - vektorZ))
                            {
                                world.setBlock(x - vektorX, y, z - vektorZ, Blocks.cobblestone);
                                int slot = inventoryContains(Blocks.cobblestone);
                                worker.getInventory().decrStackSize(slot,1);
                            }
                            else if (isValueable(x - vektorX, y, z - vektorZ))
                            {
                                Veine = new ArrayList<ChunkCoordinates>();
                                Veine.add(new ChunkCoordinates(x - vektorX, y, z - vektorZ));
                                logger.info("Found ore");

                                findVeine(x - vektorX, y, z - vektorZ);
                                logger.info("finished finding ores: " + Veine.size());

                            }
                        }
                        else if (clear % 7 == 0)
                        {
                            if (world.isAirBlock(x + vektorX, y, z + vektorZ) || !canWalkOn(x + vektorX, y, z + vektorZ))
                            {
                                world.setBlock(x + vektorX, y, z + vektorZ, Blocks.cobblestone);
                                int slot = inventoryContains(Blocks.cobblestone);
                                worker.getInventory().decrStackSize(slot,1);
                            }
                            else if (isValueable(x + vektorX, y, z + vektorZ))
                            {
                                Veine = new ArrayList<ChunkCoordinates>();
                                Veine.add(new ChunkCoordinates(x + vektorX, y, z + vektorZ));
                                logger.info("Found ore");
                                findVeine(x + vektorX, y, z + vektorZ);
                                logger.info("finished finding ores: " + Veine.size());

                            }
                        }

                        if (clear < 7)
                        {
                            //Do Nothing
                        }
                        else if (clear < 14)
                        {
                            if (clear == 7)
                            {
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
                            }
                        } else if (clear < 21)
                        {
                            if (clear == 14)
                            {
                                if (vektorX == 0)
                                {
                                    x += 1;
                                    z -= 7;
                                } else if (vektorZ == 0)
                                {
                                    z += 1;
                                    x -= 7;
                                }
                            }
                        } else if (clear < 28)
                        {
                            if (clear == 21)
                            {
                                if (vektorX == 0)
                                {
                                    x += 1;
                                    z -= 7;
                                } else if (vektorZ == 0)
                                {
                                    z += 1;
                                    x -= 7;
                                }
                            }
                            if (vektorX == 0)
                            {

                                if (isValueable(x + 1, y, z))
                                {
                                    Veine = new ArrayList<ChunkCoordinates>();
                                    Veine.add(new ChunkCoordinates(x + 1, y, z));
                                    logger.info("Found ore");

                                    findVeine(x + 1, y, z);
                                    logger.info("finished finding ores: " + Veine.size());

                                }
                                if (world.isAirBlock(x + 1, y, z) || !canWalkOn(x + 1, y, z))
                                {
                                    world.setBlock(x + 1, y, z, Blocks.cobblestone);
                                    int slot = inventoryContains(Blocks.cobblestone);
                                    worker.getInventory().decrStackSize(slot,1);
                                }
                            } else if (vektorZ == 0)
                            {

                                if (isValueable(x, y, z + 1))
                                {
                                    Veine = new ArrayList<ChunkCoordinates>();
                                    Veine.add(new ChunkCoordinates(x, y, z + 1));
                                    logger.info("Found ore");

                                    findVeine(x, y, z + 1);
                                    logger.info("finished finding ores: " + Veine.size());

                                }
                                if (world.isAirBlock(x, y, z + 1) || !canWalkOn(x, y, z + 1))
                                {
                                    world.setBlock(x, y, z + 1, Blocks.cobblestone);
                                    int slot = inventoryContains(Blocks.cobblestone);
                                    worker.getInventory().decrStackSize(slot,1);
                                }
                            }
                        }
                        else if (clear < 35)
                        {
                            if (clear == 28)
                            {
                                if (vektorX == 0)
                                {

                                    if (isValueable(x + 1, y, z))
                                    {
                                        Veine = new ArrayList<ChunkCoordinates>();
                                        Veine.add(new ChunkCoordinates(x + 1, y, z));
                                        logger.info("Found ore");

                                        findVeine(x + 1, y, z);
                                        logger.info("finished finding ores: " + Veine.size());

                                    }
                                    if (world.isAirBlock(x + 1, y, z) || !canWalkOn(x + 1, y, z))
                                    {
                                        world.setBlock(x + 1, y, z, Blocks.cobblestone);
                                        int slot = inventoryContains(Blocks.cobblestone);
                                        worker.getInventory().decrStackSize(slot,1);
                                    }
                                }
                                else if (vektorZ == 0)
                                {

                                    if (isValueable(x, y, z + 1))
                                    {
                                        Veine = new ArrayList<ChunkCoordinates>();
                                        Veine.add(new ChunkCoordinates(x, y, z + 1));
                                        logger.info("Found ore");

                                        findVeine(x, y, z + 1);
                                        logger.info("finished finding ores: " + Veine.size());

                                    }
                                    if (world.isAirBlock(x, y, z + 1) || !canWalkOn(x, y, z + 1))
                                    {
                                        world.setBlock(x, y, z + 1, Blocks.cobblestone);
                                        int slot = inventoryContains(Blocks.cobblestone);
                                        worker.getInventory().decrStackSize(slot,1);
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
                            }
                        } else if (clear < 42)
                        {
                            if (clear == 35)
                            {
                                if (vektorX == 0)
                                {
                                    x -= 1;
                                    z -= 7;
                                } else if (vektorZ == 0)
                                {
                                    z -= 1;
                                    x -= 7;
                                }
                            }
                        }
                        else if (clear <= 49)
                        {
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
                            if (vektorX == 0)
                            {

                                if (isValueable(x - 1, y, z))
                                {
                                    Veine = new ArrayList<ChunkCoordinates>();
                                    Veine.add(new ChunkCoordinates(x - 1, y, z));
                                    logger.info("Found ore");

                                    findVeine(x - 1, y, z);
                                    logger.info("finished finding ores: " + Veine.size());

                                }
                                if (world.isAirBlock(x - 1, y, z) || !canWalkOn(x - 1, y, z))
                                {
                                    world.setBlock(x - 1, y, z, Blocks.cobblestone);
                                    int slot = inventoryContains(Blocks.cobblestone);
                                    worker.getInventory().decrStackSize(slot,1);
                                }
                            } else if (vektorZ == 0)
                            {

                                if (isValueable(x, y, z - 1))
                                {
                                    Veine = new ArrayList<ChunkCoordinates>();
                                    Veine.add(new ChunkCoordinates(x, y, z - 1));
                                    logger.info("Found ore");

                                    findVeine(x, y, z - 1);
                                    logger.info("finished finding ores: " + Veine.size());

                                }
                                if (world.isAirBlock(x, y, z - 1) || !canWalkOn(x, y, z - 1))
                                {
                                    world.setBlock(x, y, z - 1, Blocks.cobblestone);
                                    int slot = inventoryContains(Blocks.cobblestone);
                                    worker.getInventory().decrStackSize(slot,1);
                                }
                            }
                        }

                        x = x + vektorX;
                        z = z + vektorZ;

                        if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
                        {
                            world.setBlock(x, y - 1, z, Blocks.dirt);
                            int slot = inventoryContains(Blocks.dirt);
                            worker.getInventory().decrStackSize(slot,1);
                        }

                        job.getLocation.set(x, y, z);
                        clear += 1;
                    }
                }
            }
        }
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
                    worker.setHeldItem(slot);
                    return slot;
                }
            }
        }
        return -1;
    }

    private void doMiningAnimation(Block block, int x, int y, int z)
    {

        ChunkCoordinates bk = new ChunkCoordinates(x,y,z);
        List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, bk, 0);//0 is fortune level, it doesn't matter
        for (ItemStack item : items)
        {
            InventoryUtils.setStack(worker.getInventory(), item);
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

        if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
        {
            world.setBlock(x, y - 1, z, Blocks.dirt);
            int slot = inventoryContains(Blocks.dirt);
            worker.getInventory().decrStackSize(slot,1);
        }


        world.playSoundEffect(
                (float) x + 0.5F,
                (float) y + 0.5F,
                (float) z + 0.5F,
                block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 2.0F) / 8.0F, block.stepSound.getPitch() * 0.5F);

        //TODO particles
        worker.swingItem();
        //Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(log.posX, log.posY, log.posZ, block, ChunkCoordUtils.getBlockMetadata(world, log));

        //Damage Tools
        ItemStack Tool = worker.getInventory().getHeldItem();
        if (Tool == null)
        {
            SufficientTools = false;
        }

        else
        {
            Tool.getItem().onBlockDestroyed(Tool, world, block, x, y, z, worker);//Dangerous
            if (Tool.stackSize < 1)//if axe breaks
            {
                worker.setCurrentItemOrArmor(0, null);
                worker.getInventory().setInventorySlotContents(worker.getInventory().getHeldItemSlot(), null);
                //TODO particles
            }
        }
        if (!hasAllTheTools())
        {
            SufficientTools = false;
        }

        if(Veine == null)
        {
            if (isValueable(x, y, z))
            {
                Veine = new ArrayList<ChunkCoordinates>();
                Veine.add(new ChunkCoordinates(x, y, z));
                logger.info("Found ore");

                findVeine(x, y, z);
                logger.info("finished finding ores: " + Veine.size());

            }
            else
            {
                world.setBlockToAir(x, y, z);
                delay = 20;
            }
        }
        else
        {
            world.setBlockToAir(x, y, z);
            delay = 20;

        }

        if(y < currentY)
        {
            world.setBlock(x,y,z,Blocks.cobblestone);
            int slot = inventoryContains(Blocks.cobblestone);
            worker.getInventory().decrStackSize(slot,1);
        }

    }
    private void findVeine(int x, int y, int z)
    {
            for (int x1 = x - 1; x1 <= x + 1; x1++)
            {
                for (int z1 = z - 1; z1 <= z + 1; z1++)
                {
                    for (int y1 = y - 1; y1 <= y + 1; y1++)
                    {
                        if (isValueable(x1, y1, z1))
                        {
                            ChunkCoordinates ore = new ChunkCoordinates(x1, y1, z1);
                            if (!Veine.contains(ore))
                            {
                                Veine.add(ore);
                                logger.info("Found close ore");

                            }
                        }
                    }
                }
            }

            if((VeineId < Veine.size()))
            {
                ChunkCoordinates v = Veine.get(VeineId++);
                logger.info("Check next ore");

                findVeine(v.posX, v.posY, v.posZ);
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

    private boolean isValueable(int x, int y, int z)
    {
        Block block = world.getBlock(x,y,z);
        String findOre = block.toString();

        return findOre.contains("ore") || findOre.contains("Ore");


    }

    //TODO DigDelay

    //TODO Diggable by Tool



}