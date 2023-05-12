package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.coremod.entity.ai.citizen.sawmill.EntityAIWorkSawmill;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Sawmill job.
 */
public class JobSawmill extends AbstractJobCrafter<EntityAIWorkSawmill, JobSawmill>
{
    /**
     * Instantiates the job for the Sawmill.
     *
     * @param entity the citizen who becomes a Sawmill
     */
    public JobSawmill(final ICitizenData entity)
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
    public EntityAIWorkSawmill generateAI()
    {
        return new EntityAIWorkSawmill(this);
    }

    @Override
    public @NotNull ResourceLocation getModel()
    {
        return ModModelTypes.CARPENTER_ID;
    }

    @Override
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        if (worker.getRandom().nextBoolean())
        {
            worker.queueSound(ModSoundEvents.SAW, blockPos, 10, 0);
        }
        else
        {
            worker.queueSound(SoundEvents.BAMBOO_HIT, blockPos, 5, 1);
        }
    }
}
