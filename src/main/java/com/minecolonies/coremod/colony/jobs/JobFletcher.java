package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.fletcher.EntityAIWorkFletcher;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Fletcher job.
 */
public class JobFletcher extends AbstractJobCrafter<EntityAIWorkFletcher, JobFletcher>
{
    /**
     * Instantiates the job for the Fletcher.
     *
     * @param entity the citizen who becomes a Fletcher
     */
    public JobFletcher(final ICitizenData entity)
    {
        super(entity);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkFletcher generateAI()
    {
        return new EntityAIWorkFletcher(this);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.FLETCHER_ID;
    }
}
