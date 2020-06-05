package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.research.EntityAIWorkResearcher;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the researcher.
 */
public class JobResearch extends AbstractJob<EntityAIWorkResearcher, JobResearch>
{
    /**
     * Public constructor of the researcher job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobResearch(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.researcher;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.researcher";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.STUDENT;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkResearcher generateAI()
    {
        return new EntityAIWorkResearcher(this);
    }
}
