package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.cook.EntityAIWorkCookAssistant;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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

    @Override
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        worker.queueSound(SoundEvents.FIRE_AMBIENT, blockPos, 5, 0);
        if (worker.getRandom().nextBoolean())
        {
            worker.queueSound(SoundEvents.COPPER_HIT, blockPos, 5, 0);
        }
    }
}
