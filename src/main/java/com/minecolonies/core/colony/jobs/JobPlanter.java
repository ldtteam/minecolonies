package com.minecolonies.core.colony.jobs;

import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the planter job.
 */
public class JobPlanter extends AbstractJobCrafter<EntityAIWorkPlanter, JobPlanter>
{
    /**
     * Instantiates the job for the plantation.
     *
     * @param entity the citizen who becomes a planter
     */
    public JobPlanter(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.PLANTER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkPlanter generateAI()
    {
        return new EntityAIWorkPlanter(this);
    }

    @Override
    public boolean ignoresDamage(@NotNull final DamageSource damageSource)
    {
        return damageSource == DamageSource.CACTUS;
    }
}
