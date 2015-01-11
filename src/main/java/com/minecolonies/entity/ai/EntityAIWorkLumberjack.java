package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;

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

    private static final int SEARCH_RANGE = 20;
    private static final int SEARCH_INTERVAL = 10;
    private static final int SEARCH_STEPS = 2*SEARCH_RANGE / SEARCH_INTERVAL;

    private static final int WORKER_INTERVAL = 10;
    private static final int CLUSTER_TREE_DISTANCE = 16;//square distance

    private int searchX = 0;
    private int searchZ = 0;

    private int chopTicks = 0;
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
        //if (worker.getOffsetTicks() % WORKER_INTERVAL != 0)
        //{
        //    return;
        //}

        if(delay > 0)
        {
            delay--;
            return;
        }

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
        int posX = worker.getWorkBuilding().getLocation().posX - SEARCH_RANGE + searchX*SEARCH_INTERVAL;
        int y = worker.getWorkBuilding().getLocation().posY + 2;
        int posZ = worker.getWorkBuilding().getLocation().posZ - SEARCH_RANGE + searchZ*SEARCH_INTERVAL;

        for (int x = posX; x < posX + SEARCH_INTERVAL; x++)
        {
            for (int z = posZ; z < posZ + SEARCH_INTERVAL; z++)
            {
                Block block = world.getBlock(x, y, z);
                if (block instanceof BlockLog)
                {
                    System.out.println("BlockLog found");
                    Tree t = new Tree(world, new ChunkCoordinates(x, y, z));
                    if (t.isTree())
                    {
                        System.out.println("Tree found");
                        if (!trees.contains(t))
                        {
                            System.out.println("Tree added");
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
        if (!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.tree.getLocation()))
        {
            System.out.println("Worker not at site: " + job.tree.getLocation().toString());
            System.out.println("\tDistance: " + ChunkCoordUtils.distanceSqrd(job.tree.getLocation(), worker.getPosition()));
            //TODO break leaves in way of path to tree
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
            //worker.getHeldItem().damageItem(1, worker);//TODO Doesn't work
            //getInventory().getStackInSlot(getAxeSlot()).damageItem(1, worker);//this either, Item damages but doesn't break

            //tree is gone
            if (!job.tree.hasLogs())
            {
                boolean success = plantSapling(job.tree.getLocation());
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

    private void dumpInventory()//TODO if full of saplings dump some of them too
    {
        if (ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getWorkBuilding().getLocation()))
        {
            for (int i = 0; i < getInventory().getSizeInventory(); i++)
            {
                ItemStack stack = getInventory().getStackInSlot(i);
                if (stack != null && !(stack.getItem() instanceof ItemAxe || isStackSapling(stack)))
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
            //TODO forge tool types "axe"
            if (!(worker.getHeldItem() != null && worker.getHeldItem().getItem() instanceof ItemAxe))
            {
                equipAxe();
            }
            return true;
        }
        return false;
    }

    private int getAxeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), ItemAxe.class);
    }

    private void equipAxe()
    {
        worker.setCurrentItemOrArmor(0, getInventory().getStackInSlot(getAxeSlot()));
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
                worker.setCurrentItemOrArmor(0, stack);
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
}