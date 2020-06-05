package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.builder.EntityAIStructureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * The job of the builder.
 */
public class JobBuilder extends AbstractJobStructure<EntityAIStructureBuilder, JobBuilder>
{
    /**
     * Instantiates builder job.
     *
     * @param entity citizen.
     */
    public JobBuilder(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.builder;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Builder";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.BUILDER;
    }


    @NotNull
    @Override
    public EntityAIStructureBuilder generateAI()
    {
        return new EntityAIStructureBuilder(this);
    }
}
