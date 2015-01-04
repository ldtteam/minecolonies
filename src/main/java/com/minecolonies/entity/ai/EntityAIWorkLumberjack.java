package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

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
        if (hasAxe())
        {
            equipAxe();
        }
        else
        {
            requestAxe();
        }
    }

    @Override
    public void updateTask()
    {
        //TODO work interval

        switch (job.getStage())
        {
        case IDLE:
            if (!hasAxe())
            {
                requestAxe();
            }
            else
            {
                //TODO plant, search, or wait
            }
            break;
        case SEARCHING:
            if (!hasAxe())//TODO don't really need an axe here
            {
                job.setStage(Stage.IDLE);
            }
            else
            {
                findTree();//Find tree location
            }
            break;
        case CHOPPING:
            if (!hasAxe())
            {
                job.setStage(Stage.IDLE);
            }
            else if(job.tree == null)
            {
                if(trees.size() > 0)
                {
                    job.tree = trees.poll();
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

    private int getAxeSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), ItemAxe.class);
    }

    private void equipAxe()
    {
        getEntity().setCurrentItemOrArmor(0, getInventory().getStackInSlot(getAxeSlot()));
    }

    private void requestAxe()
    {
        //TODO request by tool type
        //job.addItemNeeded();
        //TODO go home or plant trees
    }

    private void findTree()
    {
        //TODO

        //Search
        //if(isTree())
        //createTree()
    }

    private void chopTree()
    {
        //TODO walk to tree

        ChunkCoordinates log = job.tree.getNextLog();
        if(InventoryUtils.getOpenSlot(getInventory()) != -1)//inventory has an open slot - this doesn't account for slots with non full stacks
        {                                                   //also we still may have problems if the block drops multiple items
            List<ItemStack> items = ChunkCoordUtils.getBlockDrops(world, log, 0);//TODO get fortune level from axe (if it matters..)
            for(ItemStack item : items)
            {
                InventoryUtils.setStack(getInventory(), item);
            }
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

    private EntityCitizen getEntity()
    {
        return job.getCitizen().getCitizenEntity();
    }

    private InventoryCitizen getInventory()
    {
        return getEntity().getInventory();
    }

    private World getWorld()
    {
        return job.getColony().getWorld();
    }
}

