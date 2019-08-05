package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.sounds.FarmerSounds;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Job class of the farmer, handles his fields.
 */
public class JobFarmer extends AbstractJob
{
    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobFarmer(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.farmer;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Farmer";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.FARMER;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobFarmer> generateAI()
    {
        return new EntityAIWorkFarmer(this);
    }

    @Override
    public SoundEvent getBedTimeSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FarmerSounds.Female.offToBed : null;
        }
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FarmerSounds.Female.badWeather : null;
        }
        return null;
    }

    @Override
    public SoundEvent getMoveAwaySound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FarmerSounds.Female.hostile : null;
        }
        return null;
    }
}
