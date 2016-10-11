package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.guard.EntityAIGuard;
import org.jetbrains.annotations.NotNull;

/**
 * Job class of the guard.
 */
public class JobGuard extends AbstractJob
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobGuard(CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Guard";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.NOBLE;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list
     */
    @NotNull
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIGuard(this);
    }

}
