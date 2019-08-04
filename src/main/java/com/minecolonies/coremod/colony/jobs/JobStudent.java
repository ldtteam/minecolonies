package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.student.EntityAIStudy;
import org.jetbrains.annotations.NotNull;

/**
 * The student job class.
 */
public class JobStudent extends AbstractJob
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
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.student";
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobStudent> generateAI()
    {
        return new EntityAIStudy(this);
    }

    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.STUDENT;
    }
}
