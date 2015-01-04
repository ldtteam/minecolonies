package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
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
    private static final int WORKER_INTERVAL = 10;

    private Queue<Tree> trees = new LinkedList<Tree>();

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
        //TODO work interval
        if(worker.getOffsetTicks() % WORKER_INTERVAL != 0)
        {
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
                //TODO plant, search, or wait
                job.setStage(Stage.SEARCHING);
            }
            break;
        case SEARCHING:
            if (!hasAxeWithEquip())//TODO don't really need an axe here
            {
                job.setStage(Stage.IDLE);
            }
            else
            {
                findTrees();//Find tree location
            }
            break;
        case CHOPPING:
            if (!hasAxeWithEquip())
            {
                job.setStage(Stage.IDLE);
            }
            else if(job.tree == null)
            {
                if(trees.size() > 0)
                {
                    job.tree = trees.poll();//TODO find trees more efficiently
                }
                else
                {
                    job.setStage(Stage.SEARCHING);
                }
            }
            else
            {
                chopTree();//Go through Queue
            }
            break;
        case GATHERING:
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

    private boolean hasAxe()
    {
        return getAxeSlot() != -1;
    }

    private boolean hasAxeWithEquip()
    {
        if(hasAxe())
        {
            if(!(worker.getHeldItem() != null && worker.getHeldItem().getItem() instanceof ItemAxe))
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

    private void requestAxe()
    {
        //TODO request by tool type
        //job.addItemNeeded();
        //TODO go home or plant trees
    }

    private void findTrees()
    {
        //Search
        int posX = (int) worker.posX;
        int y = (int) worker.posY + 2;
        int posZ = (int) worker.posZ;

        for(int x = posX - SEARCH_RANGE; x < posX + SEARCH_RANGE; x++)
        {
            for(int z = posZ - SEARCH_RANGE; z < posZ + SEARCH_RANGE; z++)
            {
                Block block = world.getBlock(x, y, z);
                //System.out.println(block.getLocalizedName());
                if(block instanceof BlockLog)
                {
                    System.out.println("Block log found");
                    Tree t = new Tree(world, new ChunkCoordinates(x, y, z));
                    if(t.isTree())
                    {
                        System.out.println("Tree found");
                        if(!trees.contains(t))
                        {
                            System.out.println("Tree added");
                            trees.add(t);
                        }
                    }
                }
            }
        }
        System.out.println("Trees: " + trees.size());
        job.setStage(Stage.CHOPPING);
    }

    private void chopTree()
    {
        if(!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.tree.getLocation()))
        {
            System.out.println("Worker not at site: " + job.tree.getLocation().toString());
            System.out.println("\tDistance: " + ChunkCoordUtils.distanceSqrd(job.tree.getLocation(), worker.getPosition()));
            return;
        }

        ChunkCoordinates log = job.tree.getNextLog();
        if(InventoryUtils.getOpenSlot(getInventory()) != -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, log, 0);//TODO get fortune level from axe (if it matters..)
            for(ItemStack item : items)
            {
                InventoryUtils.setStack(getInventory(), item);
            }
            ChunkCoordUtils.setBlock(world, log, Blocks.air);
            worker.swingItem();
        }

        //tree is gone
        if(!job.tree.hasLogs())
        {
            //TODO plant sapling at job.tree.getLocation()

            job.tree = null;
        }
    }

    private void pickupSaplings()
    {
        //TODO
    }

    private void dumpInventory()
    {
        //TODO
    }

    private InventoryCitizen getInventory()
    {
        return worker.getInventory();
    }
}

