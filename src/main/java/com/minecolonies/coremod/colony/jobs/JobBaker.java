package com.minecolonies.coremod.colony.jobs;

import net.minecraft.util.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.baker.EntityAIWorkBaker;
import org.jetbrains.annotations.NotNull;

/**
 * The fisherman's job class. implements some useful things for him.
 */
public class JobBaker extends AbstractJobCrafter<EntityAIWorkBaker, JobBaker>
{
    /**
     * Initializes the job class.
     *
     * @param entity The entity which will use this job class.
     */
    public JobBaker(final ICitizenData entity)
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
    public ResourceLocation getModel()
    {
        return ModModelTypes.BAKER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkBaker generateAI()
    {
        return new EntityAIWorkBaker(this);
    }
}

