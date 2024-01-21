package com.minecolonies.core.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.ai.workers.crafting.EntityAIWorkStoneSmeltery;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the Stone Smeltery job.
 */
public class JobStoneSmeltery extends AbstractJobCrafter<EntityAIWorkStoneSmeltery, JobStoneSmeltery>
{
    /**
     * Instantiates the job for the Stone Smeltery.
     *
     * @param entity the citizen who becomes a Stone Smelter.
     */
    public JobStoneSmeltery(final ICitizenData entity)
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
    public EntityAIWorkStoneSmeltery generateAI()
    {
        return new EntityAIWorkStoneSmeltery(this);
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
