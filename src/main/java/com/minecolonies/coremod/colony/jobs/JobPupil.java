package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.school.EntityAIWorkPupil;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the pupil.
 */
public class JobPupil extends AbstractJob
{
    /**
     * Public constructor of the pupil job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobPupil(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.pupil;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.pupil";
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
    public AbstractAISkeleton<JobPupil> generateAI()
    {
        return new EntityAIWorkPupil(this);
    }
}
