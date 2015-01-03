package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.item.ItemAxe;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class EntityAIWorkLumberjack extends EntityAIWork<JobLumberjack>
{
    public EntityAIWorkLumberjack(JobLumberjack job)
    {
        super(job);
    }

    public enum Stage
    {
        IDLE,//No resources
        SEARCHING,
        CHOPPING,
        GATHERING,
        INVENTORY_FULL
    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute();
    }

    @Override
    public void startExecuting()
    {
        if(hasAxe())
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

        switch(job.getStage())
        {
        case IDLE:
            if(!hasAxe())
            {
                requestAxe();
            }
            else
            {
                //TODO plant, search, or wait
            }
            break;
        case SEARCHING:
            if(!hasAxe())
            {
                job.setStage(Stage.IDLE);
            }
            else
            {
                findTree();//Find tree location
            }
            break;
        case CHOPPING:
            if(!hasAxe())
            {
                job.setStage(Stage.IDLE);
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
        //TODO
        //if not near tree, walk to tree
        ChunkCoordinates log;//todo
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

