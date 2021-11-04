package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.sifter.EntityAIWorkSifter;
import org.jetbrains.annotations.NotNull;

/**
 * The sifter job class.
 */
public class JobSifter extends AbstractJobCrafter<EntityAIWorkSifter, JobSifter>
{
    /**
     * Create a sifter job.
     *
     * @param entity the lumberjack.
     */
    public JobSifter(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return ModModelTypes.smelter;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkSifter generateAI()
    {
        return new EntityAIWorkSifter(this);
    }
}
