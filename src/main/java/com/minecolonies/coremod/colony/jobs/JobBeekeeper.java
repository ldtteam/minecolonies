package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.beekeeper.EntityAIWorkBeekeeper;

/**
 * Class of the Beekeeper job.
 */
public class JobBeekeeper extends AbstractJob<EntityAIWorkBeekeeper, JobBeekeeper>
{
    /**
     * The value where when reached the counter check returns true. 100 ticks * 4 = 1 ingame day.
     */
    public static final int COUNTER_TRIGGER = 4;

    /**
     * The counting variable.
     */
    private int counter = 0;

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobBeekeeper(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * The {@link JobEntry} for this job.
     *
     * @return The {@link JobEntry}.
     */
    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.beekeeper;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Beekeeper";
    }

    /**
     * Generate your AI class to register.
     * <p>
     * Suppressing Sonar Rule squid:S1452 This rule does "Generic wildcard types should not be used in return parameters" But in this case the rule does not apply because We are
     * fine with all AbstractJob implementations and need generics only for java
     *
     * @return your personal AI instance.
     */
    @Override
    public EntityAIWorkBeekeeper generateAI()
    {
        return new EntityAIWorkBeekeeper(this);
    }

    /**
     * Tick the bee interaction counter to determine the time when the interaction gets triggered.
     */
    public void tickNoBees()
    {
        if (counter < 100) // to prevent unnecessary high counter when ignored by player
        {
            counter++;
        }
    }

    /**
     * Reset the bee interaction counter.
     */
    public void resetCounter()
    {
        counter = 0;
    }

    /**
     * Check if the interaction is valid/should be triggered.
     *
     * @return true if the interaction is valid/should be triggered.
     */
    public boolean checkForBeeInteraction()
    {
        return counter > COUNTER_TRIGGER;
    }

    @Override
    public IModelType getModel()
    {
        return BipedModelType.BEEKEEPER;
    }
}
