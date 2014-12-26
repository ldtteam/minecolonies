package com.minecolonies.entity.ai;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.entity.EntityMiner;

/**
 * Miner AI class
 * Created: December 20, 2014
 *
 * @author Colton
 */
public class EntityAIWorkMiner extends EntityAIWork<EntityMiner>
{
    public EntityAIWorkMiner(EntityMiner worker)
    {
        super(worker);
    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute() && worker.isNeeded();
    }

    @Override
    public void startExecuting()
    {
        if (!Configurations.builderInfiniteResources)
        {
            //requestMaterials();
        }

        worker.setStatus(EntityBuilder.Status.WORKING);
    }

    @Override
    public void updateTask()
    {
        if (worker.getOffsetTicks() % worker.getWorkInterval() != 0)
        {
            return;
        }

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