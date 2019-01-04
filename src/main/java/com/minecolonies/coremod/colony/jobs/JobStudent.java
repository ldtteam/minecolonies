package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
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
    public JobStudent(final CitizenData entity)
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
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.STUDENT;
    }
}
