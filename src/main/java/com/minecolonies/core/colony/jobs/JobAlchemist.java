package com.minecolonies.core.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.citizen.alchemist.EntityAIWorkAlchemist;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the alchemist job.
 */
public class JobAlchemist extends AbstractJobCrafter<EntityAIWorkAlchemist, JobAlchemist>
{
    /**
     * Instantiates the job for the alchemist.
     *
     * @param entity the citizen who becomes an alchemist
     */
    public JobAlchemist(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public EntityAIWorkAlchemist generateAI()
    {
        return new EntityAIWorkAlchemist(this);
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.ALCHEMIST_ID;
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
