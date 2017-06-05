package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.client.render.Model;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.ai.basic.AbstractAISkeleton;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * ------------ Class not Documented ------------
 */
public interface IJob
{
    /**
     * Restore the Job from an NBTTagCompound.
     *
     * @param compound NBTTagCompound containing saved Job data.
     */
    void readFromNBT(@NotNull NBTTagCompound compound);

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    String getName();

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @SideOnly(Side.CLIENT)
    Model getModel();

    /**
     * Get the CitizenData that this Job belongs to.
     *
     * @return CitizenData that owns this Job.
     */
    ICitizenData getCitizen();

    /**
     * Get the Colony that this Job is associated with (shortcut for getCitizen().getColony()).
     *
     * @return {@link IColony} of the citizen.
     */
    IColony getColony();

    /**
     * Save the Job to an NBTTagCompound.
     *
     * @param compound NBTTagCompound to save the Job to.
     */
    void writeToNBT(@NotNull NBTTagCompound compound);

    /**
     * Does the Job have _all_ the needed items.
     *
     * @return true if the Job has no needed items.
     */
    boolean isMissingNeededItem();

    /**
     * Method used to create a request in the workers building.
     *
     * @param request   The request to create.
     * @param <Request> The type of request.
     */
    <Request> void createRequest(@NotNull Request request);

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     *
     * @param tasks EntityAITasks list to add tasks to.
     */
    void addTasks(@NotNull EntityAITasks tasks);

    /**
     * Generate your AI class to register.
     * <p>
     * Suppressing Sonar Rule squid:S1452
     * This rule does "Generic wildcard types should not be used in return parameters"
     * But in this case the rule does not apply because
     * We are fine with all AbstractJob implementations and need generics only for java
     *
     * @return your personal AI instance.
     */
    @SuppressWarnings("squid:S1452")
    AbstractAISkeleton<? extends IJob> generateAI();

    /**
     * This method can be used to display the current status.
     * That a citizen is having.
     *
     * @return Small string to display info in name tag
     */
    String getNameTagDescription();

    /**
     * Used by the AI skeleton to change a citizens name.
     * Mostly used to update debugging information.
     *
     * @param nameTag The name tag to display.
     */
    void setNameTag(String nameTag);

    /**
     * Override this to let the worker return a bedTimeSound.
     *
     * @return soundEvent to be played.
     */
    SoundEvent getBedTimeSound();

    /**
     * Override this to let the worker return a badWeatherSound.
     *
     * @return soundEvent to be played.
     */
    SoundEvent getBadWeatherSound();
}
