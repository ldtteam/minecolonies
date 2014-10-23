package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityWorker;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import static com.minecolonies.entity.EntityCitizen.Status.IDLE;

public abstract class EntityAIWork<WORKER extends EntityWorker> extends EntityAIBase
{
    protected final WORKER       worker;
    protected final World        world;

    public EntityAIWork(WORKER worker)
    {
        setMutexBits(3);
        this.worker = worker;
        this.world = worker.worldObj;
    }

    @Override
    public boolean shouldExecute()
    {
        return worker.getDesiredActivity() == EntityCitizen.DesiredActivity.WORK;
    }

    @Override
    public void resetTask()
    {
        worker.setStatus(IDLE);
    }
}
