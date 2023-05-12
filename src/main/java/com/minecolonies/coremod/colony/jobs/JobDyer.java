package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.dyer.EntityAIWorkDyer;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the dyer job.
 */
public class JobDyer extends AbstractJobCrafter<EntityAIWorkDyer, JobDyer>
{
    /**
     * Instantiates the job for the Dyer.
     *
     * @param entity the citizen who becomes a dyer.
     */
    public JobDyer(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.DYER_ID;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public EntityAIWorkDyer generateAI()
    {
        return new EntityAIWorkDyer(this);
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
            worker.queueSound(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, blockPos, 5, 0);
            worker.queueSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, blockPos, 5, 0);
        }
    }
}
