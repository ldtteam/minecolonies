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
        /*
        Rough outline:
            Data structures:
                Nodes are 5x5x4 (3 tall + a ceiling)
                connections are 3x5x3 tunnels
            Connections should be automatically completed when moving from node to node

            max level depth depends on current hut level
                example:
                    1: y=44
                    2: y=28
                    3: y=10
                Personally I think our lowest level should be 4 or 5, whatever one you can't run into bedrock on

            If the miner has a node, then he should create the connection, then mine the node

            else findNewNode

            That's basically it...
            Also note we need to check the tool durability and for torches,
                wood for building the tunnel structure (exact plan to be determined)

            You also may want to create another node status before AVAILABLE for when the connection isn't completed.
                Maybe even two status, one for can_connect_too then connection_in_progress
         */
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

    private void findNewNode()
    {
        //TODO
    }
}