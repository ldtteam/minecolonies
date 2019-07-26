package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.BipedModelType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.builder.EntityAIStructureBuilder;
import com.minecolonies.coremod.sounds.BuilderSounds;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The job of the builder.
 */
public class JobBuilder extends AbstractJobStructure
{
    /**
     * Instantiates builder job.
     *
     * @param entity citizen.
     */
    public JobBuilder(final ICitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Builder";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.BUILDER;
    }


    @NotNull
    @Override
    public AbstractAISkeleton<JobBuilder> generateAI()
    {
        return new EntityAIStructureBuilder(this);
    }

    @Nullable
    @Override
    public SoundEvent getBedTimeSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? BuilderSounds.Female.offToBed : null;
        }
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? BuilderSounds.Female.badWeather : null;
        }
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getMoveAwaySound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? BuilderSounds.Female.hostile : null;
        }
        return null;
    }
}
