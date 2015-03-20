package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

    private ChunkCoordinates ladderLocation;
    private ChunkCoordinates cobbleLocation;
    private ChunkCoordinates getLocation;

    private boolean foundLadder = false;
    private boolean cleared = false;

    private int clear = 1;
    private int vektorx = 1;
    private int vektorz = 1;

    private int delay = 0;

    private boolean SufficientTools = false;


    private List<Level> levels;
    //Node Position if Node is close to x+5 or z+5 to other Node delete them Both

    private static final int MAXIMUM_LEVEL = 4; //save in hut

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
            return;
        }
        else if (!SufficientTools)
        {
            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))//Go Home
            {
                logger.info("Need tools");
                if (hasAllTheTools())
                {
                    SufficientTools = true;
                } // Add Torches
            }
        }
        else if (!foundLadder)
        {
            findLadder();
        }
        else if (!cleared)//has ladder, tools and not clear
        { //TODO Only per Hut
            createShaft(getLocation, vektorx, vektorz);
        }

        //go to Chest

        //TODO Miner AI
        /*
        Rough outline:
            Data structures:
                Nodes are 5x5x4 (3 tall + a ceiling)
                connections are 3x5x3 tunnels
            Connections should be automatically completed when moving from node to node

            max startingLevel depth depends on current hut startingLevel
                example:
                    1: y=44
                    2: y=28
                    3: y=10
                Personally I think our lowest startingLevel should be 4 or 5, whatever one you can't run into bedrock on

            If the miner has a node, then he should create the connection, then mine the node

            else findNewNode

            That's basically it...
            Also note we need to check the tool durability and for torches,
                wood for building the tunnel structure (exact plan to be determined)

            You also may want to create another node status before AVAILABLE for when the connection isn't completed.
                Maybe even two status, one for can_connect_too then connection_in_progress
         */
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
                    if (foundLadder)
                        return;

                    if (world.getBlock(x, y, z).equals(Blocks.ladder))//Parameters unused
                    {
                        if (ladderLocation == null)
                        {
                            int lastY = getLastLadder(x, y, z);
                            ladderLocation = new ChunkCoordinates(x, lastY, z);
                            posYWorker = lastY;
                            logger.info("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                        }

                        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ladderLocation))
                        {
                            cobbleLocation = new ChunkCoordinates(x, posYWorker, z);

                            //Cobble on x+1 x-1 z+1 or z-1 to the ladder
                            if (world.getBlock(ladderLocation.posX - 1, ladderLocation.posY, ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                cobbleLocation = new ChunkCoordinates(x - 1, posYWorker, z);
                                vektorx = 1;
                                vektorz = 0;
                                logger.info("Found cobble - West");
                                //West

                            }
                            else if (world.getBlock(ladderLocation.posX + 1, ladderLocation.posY, ladderLocation.posZ).equals(Blocks.cobblestone))//Parameters unused
                            {
                                cobbleLocation = new ChunkCoordinates(x + 1, posYWorker, z);
                                vektorx = -1;
                                vektorz = 0;
                                logger.info("Found cobble - East");
                                //East

                            }
                            else if (world.getBlock(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ - 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                cobbleLocation = new ChunkCoordinates(x, posYWorker, z - 1);
                                vektorz = 1;
                                vektorx = 0;
                                logger.info("Found cobble - South");
                                //South

                            }
                            else if (world.getBlock(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ + 1).equals(Blocks.cobblestone))//Parameters unused
                            {
                                cobbleLocation = new ChunkCoordinates(x, posYWorker, z + 1);
                                vektorz = -1;
                                vektorx = 0;
                                logger.info("Found cobble - North");
                                //North
                            }
                            //world.setBlockToAir(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                            getLocation = new ChunkCoordinates(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                            foundLadder = true;
                        }
                    }
                }
            }
        }
    }

    //Clearing levels + setting Ladders + Checking under and aside for empty Blocks
    private void createShaft(ChunkCoordinates getLocation, int vektorX, int vektorZ)
    {
        int x = getLocation.posX;
        int y = getLocation.posY;
        int z = getLocation.posZ;
        if (job.startingLevel % 5 == 0 && job.startingLevel != 0)
        {
            if (clear < 50)
            {
                if (clear == 1)
                {

                }
                else if (clear == 2)
                {
                    world.setBlock(x, y + 3, z, job.floorBlock);
                    world.setBlock(x, y + 4, z, job.fenceBlock);
                }
                else if (clear <= 5)
                {

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
                        }
                        else if (vektorZ == 0)
                        {
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
                    else if (clear == 30)
                    {
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
                }
                else if (clear <= 42)
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
                        }
                        else if (vektorZ == 0)
                        {
                            z -= 1;
                            x -= 7;
                        }
                    }
                    world.setBlock(x, y + 3, z, job.floorBlock);
                }

                x = x + vektorX;
                z = z + vektorZ;

                getLocation.set(x, y, z);
                clear += 1;
            }
            else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ladderLocation))
            {
                //Save Node
                //(x-4, y+1, z) (x, y+1, Z+4) and (x+4, y+1, z)
                levels = new ArrayList<Level>();
                levels.add(new Level(y + 3));//FIXME nothing is done with this
                clear = 1;
                job.startingLevel++;
                getLocation.set(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
            }
        }
        else if (clear >= 50)
        {
            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ladderLocation))
            {
                int meta = world.getBlockMetadata(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ);
                cobbleLocation.set(cobbleLocation.posX, ladderLocation.posY - 1, cobbleLocation.posZ);
                ladderLocation.set(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                world.setBlock(cobbleLocation.posX, cobbleLocation.posY, cobbleLocation.posZ, Blocks.cobblestone);
                world.setBlock(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ, Blocks.ladder, meta, 0x3);

                if (y <= MAXIMUM_LEVEL)
                {
                    cleared = true;
                }
                clear = 1;
                job.startingLevel++;
                getLocation.set(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
            }
        }
        else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, new ChunkCoordinates(x, y, z)))
        {

            worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());

            //if (!world.getBlock(x,y,z).isAir(world,x,y,z))
            {
                //TODO Keep Ressources - use Ressources
                //TODO If find ores, get all ores next to it as well
                //TODO After he went 4 levels down, set next startingLevel to wood
                if (!world.getBlock(x, y, z).isAir(world, x, y, z))
                {
                    doMiningAnimation(world.getBlock(x, y, z), x, y, z);
                    world.setBlockToAir(x, y, z);
                    delay = 20;

                    if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
                    {
                        world.setBlock(x, y - 1, z, Blocks.dirt);
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
                            world.setBlock(x - vektorX, y, z - vektorZ, Blocks.cobblestone);
                        }
                    }
                    else if (clear % 7 == 0)
                    {
                        if (world.isAirBlock(x + vektorX, y, z + vektorZ) || !canWalkOn(x + vektorX, y, z + vektorZ))
                        {
                            world.setBlock(x + vektorX, y, z + vektorZ, Blocks.cobblestone);
                        }
                    }
                    if (clear < 7)
                    {
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
                    }
                    else if (clear < 21)
                    {
                        if (clear == 14)
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
                    }
                    else if (clear < 28)
                    {
                        if (clear == 21)
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
                        if (vektorX == 0)
                        {
                            if (world.isAirBlock(x + 1, y, z) || !canWalkOn(x + 1, y, z))
                            {
                                world.setBlock(x + 1, y, z, Blocks.cobblestone);
                            }
                        }
                        else if (vektorZ == 0)
                        {
                            if (world.isAirBlock(x, y, z + 1) || !canWalkOn(x, y, z + 1))
                            {
                                world.setBlock(x, y, z + 1, Blocks.cobblestone);
                            }
                        }
                    }
                    else if (clear < 35)
                    {
                        if (clear == 28)
                        {
                            if (vektorX == 0)
                            {
                                if (world.isAirBlock(x + 1, y, z) || !canWalkOn(x + 1, y, z))
                                {
                                    world.setBlock(x + 1, y, z, Blocks.cobblestone);
                                }
                            }
                            else if (vektorZ == 0)
                            {
                                if (world.isAirBlock(x, y, z + 1) || !canWalkOn(x, y, z + 1))
                                {
                                    world.setBlock(x, y, z + 1, Blocks.cobblestone);
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
                    }
                    else if (clear < 42)
                    {
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
                            if (world.isAirBlock(x - 1, y, z) || !canWalkOn(x - 1, y, z))
                            {
                                world.setBlock(x - 1, y, z, Blocks.cobblestone);
                            }
                        }
                        else if (vektorZ == 0)
                        {
                            if (world.isAirBlock(x, y, z - 1) || !canWalkOn(x, y, z - 1))
                            {
                                world.setBlock(x, y, z - 1, Blocks.cobblestone);
                            }
                        }
                    }

                    x = x + vektorX;
                    z = z + vektorZ;

                    if (world.isAirBlock(x, y - 1, z) || !canWalkOn(x, y - 1, z))
                    {
                        world.setBlock(x, y - 1, z, Blocks.dirt);
                    }

                    getLocation.set(x, y, z);
                    clear += 1;
                }
            }
        }
    }

    private void doMiningAnimation(Block block, int x, int y, int z)
    {
        if (block == Blocks.dirt || block == Blocks.gravel || block == Blocks.sand || block == Blocks.clay || block == Blocks.grass)
        {
            holdShovel();
        }
        else
        {
            holdPickAxe();
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
        return world.getBlock(x, y, z).getMaterial().isSolid();
    }

    //TODO DigDelay

    //TODO Diggable by Tool

    //TODO On Death Safe current status in hut
    //TODO On Create get current status
    private void findNewNode()
    {
        //TODO
    }
}