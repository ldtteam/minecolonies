package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobMiner;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Colton
 */
public class EntityAIWorkMiner extends EntityAIWork<JobMiner>
{
    public EntityAIWorkMiner(JobMiner job)
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
        if (!Configurations.builderInfiniteResources)
        {
            //requestMaterials();
        }

        worker.setStatus(EntityCitizen.Status.WORKING);
    }

    @Override
    public void updateTask()
    {
        //TODO Miner AI
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
    }
}