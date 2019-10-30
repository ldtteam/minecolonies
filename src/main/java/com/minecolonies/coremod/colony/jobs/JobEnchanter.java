package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.enchanter.EntityAIWorkEnchanter;
import org.jetbrains.annotations.NotNull;

public class JobEnchanter extends AbstractJob
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobEnchanter(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.enchanter;
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.enchanter";
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
        return BipedModelType.STUDENT;
    }

    @Override
    public AbstractAISkeleton<? extends AbstractJob> generateAI()
    {
        return new EntityAIWorkEnchanter(this);
    }
}
