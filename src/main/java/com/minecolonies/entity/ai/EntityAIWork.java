package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityWorker;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

public abstract class EntityAIWork extends EntityAIBase
{
    private final   EntityWorker worker;
    protected final World        world;

    public EntityAIWork(EntityWorker worker)
    {
        setMutexBits(3);
        this.worker = worker;
        this.world = worker.worldObj;
    }

    @Override
    public boolean shouldExecute()
    {
        return worker.isWorkTime();
    }

    @Override
    public void resetTask()
    {
        if(!continueExecuting())
        {
            worker.setStatus(EntityWorker.Status.IDLE);
        }
    }
}
