package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.jobs.ColonyJob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import static com.minecolonies.entity.EntityCitizen.Status.IDLE;

public abstract class EntityAIWork<JOB extends ColonyJob> extends EntityAIBase
{
    protected final JOB           job;
    protected final EntityCitizen worker;
    protected final World         world;

    public EntityAIWork(JOB job)
    {
        setMutexBits(3);
        this.job = job;
        this.worker = this.job.getCitizen();
        this.world = this.worker.worldObj;
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
