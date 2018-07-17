package com.minecolonies.coremod.entity.ai.citizen.composter;

import com.minecolonies.coremod.colony.jobs.JobComposter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.START_WORKING;
import static com.minecolonies.coremod.entity.ai.util.AIState.COMPOSTER_FILL;
import static com.minecolonies.coremod.entity.ai.util.AIState.COMPOSTER_HARVEST;

public class EntityAIWorkComposter extends AbstractEntityAIInteract<JobComposter>
{
    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkComposter(@NotNull final JobComposter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(COMPOSTER_FILL, this::fillBarrels),
          new AITarget(COMPOSTER_HARVEST, this::harvestBarrels)
        );

        worker.setCanPickUpLoot(true);
    }

    private AIState fillBarrels()
    {
        //TODO implement
        return START_WORKING;
    }

    private AIState harvestBarrels()
    {
        //TODO implement
        return START_WORKING;
    }
}
