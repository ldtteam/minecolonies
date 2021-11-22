package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.student.EntityAIStudy;
import org.jetbrains.annotations.NotNull;

/**
 * The student job class.
 */
public class JobStudent extends AbstractJob<EntityAIStudy, JobStudent>
{
    /**
     * Create a cook job.
     *
     * @param entity the student.
     */
    public JobStudent(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIStudy generateAI()
    {
        return new EntityAIStudy(this);
    }

    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.STUDENT;
    }
}
