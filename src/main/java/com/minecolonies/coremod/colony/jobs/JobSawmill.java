package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
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
        worker.queueSound(SoundEvents.ARMOR_EQUIP_IRON, blockPos, 1, 0);
        worker.queueSound(SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, blockPos, 1, 0);
        worker.queueSound(SoundEvents.IRON_DOOR_OPEN, blockPos, 1, 0);
    }
}
