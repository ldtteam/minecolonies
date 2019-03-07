package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.sifter.EntityAIWorkSifter;
import org.jetbrains.annotations.NotNull;

/**
 * The sifter job class.
 */
public class JobSifter extends AbstractJobCrafter
{
    /**
     * Create a sifter job.
     *
     * @param entity the lumberjack.
     */
    public JobSifter(final CitizenData entity)
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
        return "com.minecolonies.coremod.job.sifter";
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.SMELTER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobSifter> generateAI()
    {
        return new EntityAIWorkSifter(this);
    }
}
