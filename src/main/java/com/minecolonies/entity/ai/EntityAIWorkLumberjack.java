package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.*;

public class EntityAIWorkLumberjack extends EntityAIWork<JobLumberjack>
{
    public enum Stage
    {
        IDLE,//No resources
        SEARCHING,
        CHOPPING,
        GATHERING,
        INVENTORY_FULL
    }

    private static final String TOOL_TYPE_AXE = "axe";

    private static final int SEARCH_RANGE = 20;
    private static final int SEARCH_INTERVAL = 10;
    private static final int SEARCH_STEPS = 2*SEARCH_RANGE / SEARCH_INTERVAL;

    private static final int CLUSTER_TREE_DISTANCE = 9;//square distance

    private int searchX = 0;
    private int searchZ = 0;

    private int chopTicks = 0;
    private int stillTicks = 0;
    private int previousDistance = 0;
    private int delay = 0;

    private List<Tree> trees = new ArrayList<Tree>();
    private List<List<Tree>> clusters = new ArrayList<List<Tree>>();

    public EntityAIWorkLumberjack(JobLumberjack job)
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
        if (!hasAxeWithEquip())
        {
            requestAxe();
        }
    }

    @Override
    public void updateTask()
    {
        if(delay > 0)
        {
            delay--;
            return;
        }

        worker.setRenderSpecial(hasLogs());

        switch (job.getStage())
        {
        case IDLE:
            if (!hasAxeWithEquip())
            {
                requestAxe();
            }
            else
            {
                job.setStage(Stage.SEARCHING);
            }
            break;
        case SEARCHING:
            if(clusters.isEmpty())
            {
                findTrees();//Do tree search
            }
            else
            {
                job.setStage(Stage.CHOPPING);
            }

            break;
        case CHOPPING:
            if (!hasAxeWithEquip())
            {
                job.setStage(Stage.IDLE);
            }
            else if (job.tree == null)
            {
                if(clusters.size() > 0)
                {
                    if(clusters.get(0).size() > 0)
                    {
                        job.tree = clusters.get(0).remove(0);
                    }
                    else
                    {
                        clusters.remove(0);
                    }
                }
                else if (trees.size() > 0)
                {
                    createTreeClusters();
                }
                else
                {
                    job.setStage(Stage.SEARCHING);
                }
            }
            else
            {
                chopTree();
            }
            break;
        //Entities now pick up nearby items
        case GATHERING://TODO never happens
            //TODO also pick up apples
            pickupSaplings();
            break;
        case INVENTORY_FULL:
            dumpInventory();
            break;
        default:
            System.out.println("Invalid stage in EntityAIWorkLumberjack");
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    @Override
    public void resetTask()
    {
        job.setStage(Stage.IDLE);
    }

    private void requestAxe()
    {
        //TODO request by tool type
        //job.addItemNeeded();
        //TODO go home or plant trees
    }

    //Splits search area into
    private void findTrees()
    {
        //TODO search a larger y range?
        int posX = worker.getWorkBuilding().getLocation().posX - SEARCH_RANGE + searchX*SEARCH_INTERVAL;
        int y = worker.getWorkBuilding().getLocation().posY + 2;
        int posZ = worker.getWorkBuilding().getLocation().posZ - SEARCH_RANGE + searchZ*SEARCH_INTERVAL;

        for (int x = posX; x < posX + SEARCH_INTERVAL; x++)
        {
            for (int z = posZ; z < posZ + SEARCH_INTERVAL; z++)
            {
                Block block = world.getBlock(x, y, z);
                if (block.isWood(world, x, y, z))//Parameters unused
                {
                    Tree t = new Tree(world, new ChunkCoordinates(x, y, z));
                    if (t.isTree())
                    {
                        if (!trees.contains(t))
                        {
                            trees.add(t);
                        }
                    }
                }
            }
        }
        System.out.println("Trees: " + trees.size());

        searchX++;
        if(searchX == SEARCH_STEPS)
        {
            searchX = 0;
            searchZ++;
            if(searchZ == SEARCH_STEPS)
            {
                //TODO is trees.size() == 0 idle, gather, plant, or broaden search
                searchZ = 0;
                job.setStage(Stage.CHOPPING);
            }
        }
    }

    private void chopTree()
    {
        if (InventoryUtils.getOpenSlot(getInventory()) == -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            job.setStage(Stage.INVENTORY_FULL);
            return;
        }

        ChunkCoordinates location = job.tree.getLocation();
        if (!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, location))
        {
            int distance = (int) ChunkCoordUtils.distanceSqrd(location, worker.getPosition());
            if(previousDistance == distance)
            {
                stillTicks++;
                if(stillTicks >= 10)
                {
                    Vec3 workerStanding = Vec3Utils.vec3Floor(worker.getPosition());
                    Vec3 treeDirection = workerStanding.subtract(Vec3.createVectorHelper(location.posX, location.posY + 2, location.posZ)).normalize();
                    //Vec3 leaves = worker.getPosition().addVector(treeDirection.xCoord, 1.0, treeDirection.zCoord);

                    int x = MathHelper.floor_double(worker.posX);
                    int y = MathHelper.floor_double(worker.posY)+1;
                    int z = MathHelper.floor_double(worker.posZ);
                    if(treeDirection.xCoord > 0.5F)
                    {
                        x++;
                    }
                    else if(treeDirection.xCoord < -0.5F)
                    {
                        x--;
                    }
                    else if(treeDirection.zCoord > 0.5F)
                    {
                        z++;
                    }
                    else if(treeDirection.zCoord < -0.5F)
                    {
                        z--;
                    }
                    if(treeDirection.yCoord > 0.75F)
                    {
                        y++;
                    }
                    else if(treeDirection.yCoord < -0.75F)
                    {
                        y--;
                    }

                    Block block = world.getBlock(x, y, z);
                    System.out.println(String.format("Block: %s  x:%d y:%d z:%d", block.getUnlocalizedName(), x, y, z));
                    if(block.isLeaves(world, x, y, z))//Parameters not used
                    {
                        world.setBlockToAir(x, y, z);
                        world.playSoundEffect(
                                (float) x + 0.5F,
                                (float) y + 0.5F, (float) z + 0.5F, block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());

                        //TODO particles
                        //TODO drops
                        worker.swingItem();

                        stillTicks = 0;
                    }
                }
            }
            else
            {
                System.out.println("Worker not at site: " + location.toString());
                System.out.println("\tDistance: " + distance);
                stillTicks = 0;
                previousDistance = distance;
            }
            return;
        }

        //TODO optimize
        if(chopTicks == 20)//log break
        {
            ChunkCoordinates log = job.tree.pollNextLog();
            Block block = ChunkCoordUtils.getBlock(world, log);
            List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, log, 0);//0 is fortune level, it doesn't matter
            for (ItemStack item : items)
            {
                InventoryUtils.setStack(getInventory(), item);
            }
            ChunkCoordUtils.setBlock(world, log, Blocks.air);
            world.playSoundEffect(
                    (float) log.posX + 0.5F,
                    (float) log.posY + 0.5F, (float) log.posZ + 0.5F, block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());
            //TODO particles
            ItemStack axe = worker.getInventory().getHeldItem();
            axe.getItem().onBlockDestroyed(axe, world, block, log.posX, log.posY, log.posZ, worker);
            if(axe.stackSize < 1)
            {
                worker.setCurrentItemOrArmor(0, null);
                getInventory().setInventorySlotContents(getInventory().getHeldItemSlot(), null);
                //TODO particles
            }

            //tree is gone
            if (!job.tree.hasLogs())
            {
                boolean success = plantSapling(location);
                System.out.println("Tree planting success: " + success);
                job.tree = null;
            }
            chopTicks = -1;//will be increased to 0 at the end of the method
        }
        else if(chopTicks % 5 == 0)//time to swing and play sounds
        {
            ChunkCoordinates log = job.tree.peekNextLog();
            if(chopTicks == 0)
            {
                worker.getLookHelper().setLookPosition(log.posX, log.posY, log.posZ, 10f, worker.getVerticalFaceSpeed());//TODO doesn't work right
            }
            Block block = ChunkCoordUtils.getBlock(world, log);
            worker.swingItem();
            world.playSoundEffect((float)log.posX + 0.5F, (float)log.posY + 0.5F, (float)log.posZ + 0.5F, block.stepSound.getStepResourcePath(), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getPitch() * 0.5F);
            //TODO particles
        }
        chopTicks++;
    }

    private void pickupSaplings()
    {
        //TODO
    }

    private void dumpInventory()
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            int saplingStacks = 0;
            for (int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if (stack != null && !isStackAxe(stack))
                {
                    if(isStackSapling(stack) && saplingStacks < 5)
                    {
                        saplingStacks++;
                    }
                    else
                    {
                        ItemStack returnStack = InventoryUtils.setStack(worker.getWorkBuilding().getTileEntity(), stack);
                        if (returnStack == null)
                        {
                            getInventory().decrStackSize(i, stack.stackSize);
                        }
                        else
                        {
                            getInventory().decrStackSize(i, stack.stackSize - returnStack.stackSize);
                        }
                    }
                }
            }
            job.setStage(Stage.IDLE);
        }
    }

    private InventoryCitizen getInventory()
    {
        return worker.getInventory();
    }

    private boolean hasAxe()
    {
        return getAxeSlot() != -1;
    }

    private boolean hasAxeWithEquip()
    {
        if (hasAxe())
        {
            if (!isStackAxe(worker.getHeldItem()))
            {
                equipAxe();
            }
            return true;
        }
        return false;
    }

    private int getAxeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_AXE);
    }

    private void equipAxe()
    {
        worker.setHeldItem(getAxeSlot());
    }

    private boolean isStackAxe(ItemStack stack)
    {
        return stack != null && stack.getItem().getToolClasses(null /* not used */).contains(TOOL_TYPE_AXE);
    }

    private boolean isStackSapling(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a instanceof BlockSapling;
    }

    private int getSaplingSlot()
    {
        for (int i = 0; i < getInventory().getSizeInventory(); i++)
        {
            if (isStackSapling(getInventory().getStackInSlot(i)))
            {
                return i;
            }
        }
        return -1;
    }

    private boolean plantSapling(ChunkCoordinates location)
    {
        int slot = getSaplingSlot();
        if (slot != -1)
        {
            ItemStack stack = getInventory().getStackInSlot(slot);
            if (stack.getItem() instanceof ItemBlock)
            {
                Block block = ((ItemBlock) stack.getItem()).field_150939_a;
                worker.setHeldItem(slot);
                if (ChunkCoordUtils.setBlock(world, location, block, stack.getItemDamage(), 0x02))
                {
                    worker.swingItem();
                    world.playSoundEffect(
                            (float) location.posX + 0.5F,
                            (float) location.posY + 0.5F, (float) location.posZ + 0.5F, block.stepSound.getBreakSound(), block.stepSound.getVolume(), block.stepSound.getPitch());
                    getInventory().decrStackSize(slot, 1);
                    delay = 10;
                    return true;
                }
            }
        }
        return false;
    }

    private void createTreeClusters()
    {
        while (!trees.isEmpty())
        {
            //create a new cluster
            List<Tree> cluster = new ArrayList<Tree>();
            //cluster queue
            Queue<Tree> clusterQueue = new LinkedList<Tree>();
            cluster.add(trees.remove(0));
            clusterQueue.add(cluster.get(0));
            clusters.add(cluster);

            if (trees.isEmpty()) break;

            int count = 0;
            //  Gather more trees into the Cluster
            while(!clusterQueue.isEmpty())
            {
                System.out.println("Times: " + ++count);
                Tree tree = clusterQueue.poll();

                Iterator<Tree> it = trees.iterator();
                while (it.hasNext())
                {
                    Tree other = it.next();

                    if (tree.squareDistance(other) < CLUSTER_TREE_DISTANCE)
                    {
                        clusterQueue.add(other);
                        cluster.add(other);
                        it.remove();
                    }
                }
                System.out.println("Cluster size: " + cluster.size());
            }
        }
        System.out.println("Clusters: " + clusters.size());

        //Sort clusters by size
        Collections.sort(clusters, new Comparator<List<Tree>>()
        {
            @Override
            public int compare(List<Tree> cluster1, List<Tree> cluster2)
            {
                return cluster1.size() - cluster2.size();
            }
            //Maybe we should sort equal clusters based on distance to previous cluster
        });
    }

    private boolean hasLogs()
    {
        for(int i = 0; i < getInventory().getSizeInventory(); i++)
        {
            if(isStackLog(getInventory().getStackInSlot(i)))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isStackLog(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a.isWood(null, 0, 0, 0);
    }
}