package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.baker.EntityAIWorkBaker;
//todo WHAAAT?!?!?!
import com.minecolonies.coremod.sounds.FishermanSounds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;


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
     * Restore the Job from an NBTTagCompound.
     *
     * @param compound NBTTagCompound containing saved Job data.
     */
    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
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
        return "com.minecolonies.coremod.job.baker";
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
        return RenderBipedCitizen.Model.BUILDER;
    }

    /**
     * Save the Job to an NBTTagCompound.
     *
     * @param compound NBTTagCompound to save the Job to.
     */
    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
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

    //todo WHAT?!?!?!
    @Override
    public SoundEvent getBedTimeSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FishermanSounds.Female.offToBed : FishermanSounds.Male.offToBed;
        }
        return null;
    }

    /**
     * Override this to let the worker return a badWeatherSound.
     *
     * @return soundEvent to be played.
     */
    //todo WHAT?!?!?!
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? FishermanSounds.Female.badWeather : FishermanSounds.Male.badWeather;
        }
        return null;
    }
}

