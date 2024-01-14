package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.citizen.cook.EntityAIWorkCook;
import org.jetbrains.annotations.NotNull;

/**
 * The cook job class.
 */
public class JobCook extends AbstractJob<EntityAIWorkCook, JobCook>
{
    /**
     * Create a cook job.
     *
     * @param entity the lumberjack.
     */
    public JobCook(final ICitizenData entity)
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
        return ModModelTypes.COOK_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkCook generateAI()
    {
        return new EntityAIWorkCook(this);
    }
}
