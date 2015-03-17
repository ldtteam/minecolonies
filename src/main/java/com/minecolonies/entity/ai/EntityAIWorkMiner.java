package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.sun.javafx.css.parser.LadderConverter;
import com.sun.xml.internal.bind.v2.TODO;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Colton
 */
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAIWorkMiner extends EntityAIWork<JobMiner> {
    public static Logger logger = LogManager.getLogger("Miner");
    boolean foundLadder = false;
    ChunkCoordinates ladderLocation;
    ChunkCoordinates cobbleLocation;
    ChunkCoordinates getLocation;
    boolean cleared = false;
    int clear = 1;
    int vektorx = 1;
    int vektorz = 1;
    int delay = 0;
    //Node Position
    //Work Position

    public EntityAIWorkMiner(JobMiner job) {
        super(job);
    }

    @Override
    public boolean shouldExecute() {
        return super.shouldExecute();
    }

    @Override
    public void startExecuting() {
        if (!Configurations.builderInfiniteResources) {
            //requestMaterials();
        }

        worker.setStatus(EntityCitizen.Status.WORKING);
        updateTask();
    }

    @Override
    public void updateTask() {

        //TODO Check if Ladder is still there after time
        if (delay > 0) {
            delay--;
            return;
        } else if (!foundLadder) {
            findLadder();
        } else if (foundLadder && cleared == false) { //TODO Only per Hut
            clearLevel(getLocation, vektorx, vektorz);

        }

        //go to Chest

        //TODO Miner AI
        /*
        Rough outline:
            Data structures:
                Nodes are 5x5x4 (3 tall + a ceiling)
                connections are 3x5x3 tunnels
            Connections should be automatically completed when moving from node to node

            max level depth depends on current hut level
                example:
                    1: y=44
                    2: y=28
                    3: y=10
                Personally I think our lowest level should be 4 or 5, whatever one you can't run into bedrock on

            If the miner has a node, then he should create the connection, then mine the node

            else findNewNode

            That's basically it...
            Also note we need to check the tool durability and for torches,
                wood for building the tunnel structure (exact plan to be determined)

            You also may want to create another node status before AVAILABLE for when the connection isn't completed.
                Maybe even two status, one for can_connect_too then connection_in_progress
         */
    }

    @Override
    public boolean continueExecuting() {
        return super.continueExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
    }

    // public void DigForNode(){};
    // public void createNode(){};
    // public void createShaft(){};


    private void findLadder() {
        int posX = MathHelper.floor_double(worker.getWorkBuilding().getLocation().posX);
        int posY = MathHelper.floor_double(worker.getWorkBuilding().getLocation().posY) + 2;
        int posZ = MathHelper.floor_double(worker.getWorkBuilding().getLocation().posZ);
        int posYWorker = worker.getWorkBuilding().getLocation().posY + 2;


        if (!foundLadder) {
            for (int x = posX - 10; x < posX + 15; x++) {
                for (int z = posZ - 10; z < posZ + 15; z++) {
                    for (int y = posY - 10; y < posY; y++) {

                        if (!foundLadder) {


                            if (world.getBlock(x, y, z).isLadder(world, x, y, z, null))//Parameters unused
                            {


                                if (ladderLocation == null) {
                                    int lastY = getLastLadder(x, y, z);
                                    ladderLocation = new ChunkCoordinates(x, lastY, z);
                                    posYWorker = lastY;
                                    logger.error("Found ladder at x:" + x + " y: " + lastY + " z: " + z);
                                }

                                if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ladderLocation)) {
                                    cobbleLocation = new ChunkCoordinates(x, posYWorker, z);

                                    //Cobble on x+1 x-1 z+1 or z-1 to the ladder
                                    if (world.getBlock(ladderLocation.posX - 1, ladderLocation.posY, ladderLocation.posZ).equals(Block.getBlockById(4)))//Parameters unused
                                    {
                                        cobbleLocation = new ChunkCoordinates(x - 1, posYWorker, z);
                                        vektorx = 1;
                                        vektorz = 0;
                                        logger.error("found cobble");
                                        //West

                                    }
                                    else if (world.getBlock(ladderLocation.posX + 1, ladderLocation.posY, ladderLocation.posZ).equals(Block.getBlockById(4)))//Parameters unused
                                    {
                                        cobbleLocation = new ChunkCoordinates(x + 1, posYWorker, z);
                                        vektorx = -1;
                                        vektorz = 0;
                                        logger.error("found cobble");
                                        //East

                                    }
                                    else if (world.getBlock(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ - 1).equals(Block.getBlockById(4)))//Parameters unused
                                    {
                                        cobbleLocation = new ChunkCoordinates(x, posYWorker, z - 1);
                                        vektorz = 1;
                                        vektorx = 0;
                                        logger.error("found cobble");
                                        //South

                                    }
                                    else if (world.getBlock(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ + 1).equals(Block.getBlockById(4)))//Parameters unused
                                    {
                                        cobbleLocation = new ChunkCoordinates(x, posYWorker, z + 1);
                                        vektorz = -1;
                                        vektorx = 0;
                                        logger.error("found cobble");
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
        }


    }


    //Clearing levels + setting Ladders + Checking under and aside
    private void clearLevel(ChunkCoordinates getLocation, int vektorx, int vektorz) {
        int x = getLocation.posX;
        int y = getLocation.posY;
        int z = getLocation.posZ;
        //Ugly Block following - waiting for new Pathfinding


         if(clear >= 50)
        {
            if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, ladderLocation))
            {
                int meta = world.getBlockMetadata(ladderLocation.posX,ladderLocation.posY,ladderLocation.posZ);
                cobbleLocation.set(cobbleLocation.posX, ladderLocation.posY - 1, cobbleLocation.posZ);
                ladderLocation.set(ladderLocation.posX, ladderLocation.posY - 1, ladderLocation.posZ);
                world.setBlock(cobbleLocation.posX,cobbleLocation.posY,cobbleLocation.posZ,Block.getBlockById(4));
                world.setBlock(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ, Block.getBlockById(65));
                world.setBlockMetadataWithNotify(ladderLocation.posX, ladderLocation.posY, ladderLocation.posZ, meta, 0x3);


                if(y <= 6)
                {
                    cleared = true;
                }
                clear = 1;
                getLocation.set(ladderLocation.posX , ladderLocation.posY-1, ladderLocation.posZ );
            }


        }
        else if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, new ChunkCoordinates(x, y, z))) {


            worker.getLookHelper().setLookPosition(x, y, z, 90f, worker.getVerticalFaceSpeed());


            if (!world.getBlock(x,y,z).isAir(world,x,y,z))
            {
                //TODO Nice Animation
                //TODO Keep Block
                //TODO If find ores, get all ores next to it as well
                //TODO After he went 4 levels down, set next level to wood
                delay = 20;
                world.setBlockToAir(x, y, z);
                logger.error("Mined at " + x + " " + y + " " + z);

                if (clear < 50) {

                    //Check if Block after End is empty (Block of Dungeons...)
                    if((clear-1)%7==0 )
                    {
                        if(world.isAirBlock(x - vektorx, y, z - vektorz)  || !canWalkOn(x - vektorx, y, z - vektorz))
                        {
                           world.setBlock(x-vektorx,y,z-vektorz,Block.getBlockById(4));

                        }
                    }
                    else if(clear%7==0)
                    {
                        if(world.isAirBlock(x + vektorx, y, z + vektorz)  || !canWalkOn(x + vektorx, y, z + vektorz))
                        {
                            world.setBlock(x+vektorx,y,z+vektorz,Block.getBlockById(4));

                        }
                    }
                    if (clear < 7) {
                    }
                    else if (clear < 14)
                    {


                        if(clear == 7) {
                            if (vektorx == 0) {
                                x += 1;
                                z -= 7;

                            } else if (vektorz == 0) {
                                z += 1;
                                x -= 7;
                            }
                        }
                    }
                    else if (clear < 21)
                    {
                        //Eine Seite komplett Cobble + Startpoint
                        if(clear == 14) {
                            if (vektorx == 0) {
                                x += 1;
                                z -= 7;
                            } else if (vektorz == 0) {
                                z += 1;
                                x -= 7;
                            }
                        }
                    }
                    else if (clear < 28)
                    {
                        if(clear == 21) {
                            if (vektorx == 0 ) {
                                x += 1;
                                z -= 7;
                            } else if (vektorz == 0) {
                                z += 1;
                                x -= 7;
                            }
                        }
                        if(vektorx==0)
                        {
                            if(world.isAirBlock(x+1, y, z)  || !canWalkOn(x+1, y, z))
                            {
                                world.setBlock(x+1,y,z,Block.getBlockById(4));

                            }
                        }
                        else if(vektorz==0)
                        {
                            if(world.isAirBlock(x, y, z+1)  || !canWalkOn(x, y, z+1))
                            {
                                world.setBlock(x,y,z+1,Block.getBlockById(4));

                            }
                        }
                    }
                    else if (clear < 35)
                    {

                        if(clear == 28) {
                            if (vektorx == 0) {
                                x = x - 4;
                                z -= 7;
                            } else if (vektorz == 0) {
                                z = z - 4;
                                x -= 7;
                            }

                        }
                    }
                    else if (clear < 42) {
                        if(clear == 35) {
                            if (vektorx == 0 ) {
                                x -= 1;
                                z -= 7;
                            } else if (vektorz == 0 ) {
                                z -= 1;
                                x -= 7;
                            }
                        }
                    }
                    else if (clear < 49) {
                        if(clear == 42) {
                            if (vektorx == 0) {
                                x -= 1;
                                z -= 7;
                            } else if (vektorz == 0 ) {
                                z -= 1;
                                x -= 7;
                            }
                        }
                        if(vektorx==0)
                        {
                            if(world.isAirBlock(x-1, y, z)  || !canWalkOn(x-1, y, z))
                            {
                                world.setBlock(x-1,y,z,Block.getBlockById(4));

                            }
                        }
                        else if(vektorz==0)
                        {
                            if(world.isAirBlock(x, y, z-1)  || !canWalkOn(x, y, z-1))
                            {
                                world.setBlock(x,y,z-1,Block.getBlockById(4));

                            }
                        }
                    }

                    x = x + vektorx;
                    z = z + vektorz;

                    if(world.isAirBlock(x, y-1, z) || !canWalkOn(x, y-1, z))
                    {
                        world.setBlock(x,y-1,z,Block.getBlockById(3));

                    }

                    getLocation.set(x, y, z);
                    clear += 1;
                }
            }
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
            return y+1;
        }

    }

    boolean canWalkOn(int x, int y, int z)
    {
        Block b = world.getBlock(x, y, z);

        return (b != Block.getBlockById(8) && b != Block.getBlockById(9) && b!= Block.getBlockById(10) && b!=Block.getBlockById(11) && b!=Block.getBlockById(30) && b!=Block.getBlockById(51) && b!=Block.getBlockById(50) && b!=Block.getBlockById(52) && b!=Block.getBlockById(55));
    }

    private void findNewNode() {
        //TODO
    }
}