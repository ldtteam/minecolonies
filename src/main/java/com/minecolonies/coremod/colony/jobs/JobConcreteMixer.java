package com.minecolonies.coremod.colony.jobs;

import net.minecraft.util.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.concrete.EntityAIConcreteMixer;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Concrete Mason job.
 */
public class JobConcreteMixer extends AbstractJobCrafter<EntityAIConcreteMixer, JobConcreteMixer>
{
    /**
     * Instantiates the job for the Concrete Mason.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public JobConcreteMixer(final ICitizenData entity)
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
    public EntityAIConcreteMixer generateAI()
    {
        return new EntityAIConcreteMixer(this);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.CONCRETE_MIXER_ID;
    }
}
