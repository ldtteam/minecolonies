package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.baker.EntityAIWorkBaker;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_JOB_BAKER;

/**
 * The fisherman's job class.
 * implements some useful things for him.
 */
public class JobBaker extends AbstractJob
{
    /**
     * Initializes the job class.
     *
     * @param entity The entity which will use this job class.
     */
    public JobBaker(final CitizenData entity)
    {
        super(entity);
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    @NotNull
    @Override
    public String getName()
    {
        return COM_MINECOLONIES_COREMOD_JOB_BAKER;
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.BAKER;
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobBaker> generateAI()
    {
        return new EntityAIWorkBaker(this);
    }

    /**
     * Override this to let the worker return a bedTimeSound.
     *
     * @return soundEvent to be played.
     */

    @Override
    public SoundEvent getBedTimeSound()
    {
       /** if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? BakerSounds.Female.offToBed : BakerSounds.Male.offToBed;
        }
        */
        return null;

    }

    /**
     * Override this to let the worker return a badWeatherSound.
     *
     * @return soundEvent to be played.
     */
    @Override
    public SoundEvent getBadWeatherSound()
    {
        /**
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? BakerSounds.Female.badWeather : BakerSounds.Male.badWeather;
        }
         */
        return null;
    }
}

