package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.alchemist.EntityAIAlchemist;

//TODO
public class JobAlchemist extends AbstractJob<EntityAIAlchemist, JobAlchemist>
{
    public JobAlchemist(final ICitizenData citizen)
    {
        super(citizen);
    }

    /**
     * The {@link JobEntry} for this job.
     *
     * @return The {@link JobEntry}.
     */
    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.alchemist;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @Override
    public String getName()
    {
        return null;
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
    public EntityAIAlchemist generateAI()
    {
        return new EntityAIAlchemist(this);
    }
}
