package com.minecolonies.core.colony.jobs;

import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.citizen.concrete.EntityAIConcreteMixer;
import net.minecraft.sounds.SoundEvents;
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

    @Override
    public void playSound(final BlockPos blockPos, final EntityCitizen worker)
    {
        if (worker.getRandom().nextInt(10) < 1)
        {
            worker.queueSound(SoundEvents.REDSTONE_TORCH_BURNOUT, blockPos, 10, 0);
        }
        else
        {
            worker.queueSound(SoundEvents.LAVA_POP, blockPos, 5, 0);
            worker.queueSound(SoundEvents.LAVA_AMBIENT, blockPos, 5, 0);
        }
    }
}
