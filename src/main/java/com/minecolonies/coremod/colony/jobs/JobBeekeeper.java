package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.beekeeper.EntityAIWorkBeekeeper;
import net.minecraft.nbt.INBT;

public class JobBeekeeper extends AbstractJob
{
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
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIWorkBeekeeper(this);
    }

    @Override
    public void deserializeNBT(final INBT nbt) {}
}
