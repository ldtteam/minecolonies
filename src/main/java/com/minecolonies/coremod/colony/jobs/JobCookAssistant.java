package com.minecolonies.coremod.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.cook.EntityAIWorkCookAssistant;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the CookAssistant job.
 */
public class JobCookAssistant extends AbstractJobCrafter<EntityAIWorkCookAssistant, JobCookAssistant>
{
    /**
     * Instantiates the job for the CookAssistant.
     *
     * @param entity the citizen who becomes a CookAssistant.
     */
    public JobCookAssistant(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.COOK_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkCookAssistant generateAI()
    {
        return new EntityAIWorkCookAssistant(this);
    }
}
