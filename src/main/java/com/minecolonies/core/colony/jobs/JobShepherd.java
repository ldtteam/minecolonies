package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.production.herders.EntityAIWorkShepherd;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Shepherd job
 */
public class JobShepherd extends AbstractJob<EntityAIWorkShepherd, JobShepherd>
{

    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobShepherd(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Nullable
    @Override
    public EntityAIWorkShepherd generateAI()
    {
        return new EntityAIWorkShepherd(this);
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
        return ModModelTypes.SHEEP_FARMER_ID;
    }
}
