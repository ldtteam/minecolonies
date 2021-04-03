package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.ai.citizen.archeologist.EntityAIWorkArcheologist;

public class JobArcheologist extends AbstractJobStructure<EntityAIWorkArcheologist, JobArcheologist>
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public JobArcheologist(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public EntityAIWorkArcheologist generateAI()
    {
        return new EntityAIWorkArcheologist();
    }
}
