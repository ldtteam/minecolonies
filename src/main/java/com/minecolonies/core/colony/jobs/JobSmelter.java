package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.crafting.EntityAIWorkSmelter;
import org.jetbrains.annotations.NotNull;

/**
 * The smelter job class.
 */
public class JobSmelter extends AbstractJob<EntityAIWorkSmelter, JobSmelter>
{
    /**
     * Create a smelter job.
     *
     * @param entity the lumberjack.
     */
    public JobSmelter(final ICitizenData entity)
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
        return ModModelTypes.SMELTER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkSmelter generateAI()
    {
        return new EntityAIWorkSmelter(this);
    }
}
