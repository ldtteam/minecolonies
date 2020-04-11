package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.research.EntityAIWorkResearcher;
import com.minecolonies.coremod.entity.ai.citizen.school.EntityAIWorkTeacher;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the teacher.
 */
public class JobTeacher extends AbstractJob
{
    /**
     * Public constructor of the teacher job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobTeacher(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.teacher;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.teacher";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.TEACHER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobTeacher> generateAI()
    {
        return new EntityAIWorkTeacher(this);
    }
}
