package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.dyer.EntityAIWorkDyer;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the dyer job.
 */
public class JobDyer extends AbstractJobCrafter<EntityAIWorkDyer, JobDyer>
{
    /**
     * Instantiates the job for the Dyer.
     *
     * @param entity the citizen who becomes a dyer.
     */
    public JobDyer(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public IModelType getModel()
    {
        return ModModelTypes.DYER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkDyer generateAI()
    {
        return new EntityAIWorkDyer(this);
    }
}
