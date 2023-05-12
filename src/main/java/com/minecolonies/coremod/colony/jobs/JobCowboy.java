package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.herders.EntityAIWorkCowboy;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Cowboy job
 */
public class JobCowboy extends AbstractJob<EntityAIWorkCowboy, JobCowboy>
{
    /**
     * Instantiates the placeholder job.
     *
     * @param entity the entity.
     */
    public JobCowboy(final ICitizenData entity)
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
    public EntityAIWorkCowboy generateAI()
    {
        return new EntityAIWorkCowboy(this);
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
        return ModModelTypes.COW_FARMER_ID;
    }
}
