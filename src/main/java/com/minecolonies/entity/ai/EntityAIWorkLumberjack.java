package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobLumberjack;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.item.ItemAxe;

public class EntityAIWorkLumberjack extends EntityAIWork<JobLumberjack>
{
    public EntityAIWorkLumberjack(JobLumberjack job)
    {
        super(job);
    }

    enum Stage
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

        if(!hasAxe())
        {
            requestAxe();
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
    }

    private EntityCitizen getEntity()
    {
        return job.getCitizen().getCitizenEntity();
    }

    private InventoryCitizen getInventory()
    {
        return getEntity().getInventory();
    }
}