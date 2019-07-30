package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.BipedModelType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.composter.EntityAIWorkComposter;
import org.jetbrains.annotations.NotNull;

public class JobComposter extends AbstractJob
{

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobComposter(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.composter";
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
        return BipedModelType.COMPOSTER;
    }

    @Override
    public AbstractAISkeleton<? extends IJob> generateAI()
    {
        return new EntityAIWorkComposter(this);
    }
}
