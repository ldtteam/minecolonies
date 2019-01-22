package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.registry.JobRegistry;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Suppression.CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION;
import static com.minecolonies.coremod.colony.jobs.registry.JobRegistry.TAG_TYPE;

/**
 * Basic job information.
 * <p>
 * Suppressing Sonar Rule squid:S2390
 * This rule does "Classes should not access static members of their own subclasses during initialization"
 * But in this case the rule does not apply because
 * We are only mapping classes and that is reasonable
 */
@SuppressWarnings(CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION)
public abstract class AbstractJob
{
    private static final String TAG_ASYNC_REQUESTS = "asyncRequests";
    private static final String TAG_ACTIONS_DONE   = "actionsDone";

    /**
     * A counter to dump the inventory after x actions.
     */
    private int actionsDone = 0;

    /**
     * The priority assigned with every main AI job.
     */
    private static final int TASK_PRIORITY = 3;

    /**
     * Citizen connected with the job.
     */
    private final CitizenData citizen;

    /**
     * The name tag of the job.
     */
    private String nameTag = "";

    /**
     * A set of tokens that point to requests for which we do not wait.
     */
    private final Set<IToken> asyncRequests = new HashSet<>();

    /**
     * Check if the worker has searched for food today.
     */
    private boolean searchedForFoodToday;

    /**
     * The workerAI for this Job
     */
    private WeakReference<AbstractAISkeleton> workerAI = new WeakReference<>(null);

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJob(final CitizenData entity)
    {
        citizen = entity;
    }

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    public abstract String getName();

    /**
     * Getter for the job which will be associated with the experience.
     *
     * @return the getName() or the specialized class name.
     */
    public String getExperienceTag()
    {
        return getName();
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.CITIZEN;
    }

    /**
     * Get the Colony that this Job is associated with (shortcut for getAssignedCitizen().getColonyByPosFromWorld()).
     *
     * @return {@link Colony} of the citizen.
     */
    public Colony getColony()
    {
        return citizen.getColony();
    }

    /**
     * Save the Job to an NBTTagCompound.
     *
     * @param compound NBTTagCompound to save the Job to.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        final String s = JobRegistry.getClassToNameMap().get(this.getClass());

        if (s == null)
        {
            throw new IllegalStateException(this.getClass() + " is missing a mapping! This is a bug!");
        }

        compound.setString(TAG_TYPE, s);
        compound.setTag(TAG_ASYNC_REQUESTS, getAsyncRequests().stream().map(StandardFactoryController.getInstance()::serialize).collect(NBTUtils.toNBTTagList()));
        compound.setInteger(TAG_ACTIONS_DONE, actionsDone);
    }

    /**
     * Restore the Job from an NBTTagCompound.
     *
     * @param compound NBTTagCompound containing saved Job data.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        this.asyncRequests.clear();
        if (compound.hasKey(TAG_ASYNC_REQUESTS))
        {
            this.asyncRequests.addAll(NBTUtils.streamCompound(compound.getTagList(TAG_ASYNC_REQUESTS, Constants.NBT.TAG_COMPOUND))
                                        .map(StandardFactoryController.getInstance()::deserialize)
                                        .map(o -> (IToken) o)
                                        .collect(Collectors.toSet()));
        }
        if (compound.hasKey(TAG_ACTIONS_DONE))
        {
            actionsDone = compound.getInteger(TAG_ACTIONS_DONE);
        }
    }

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    public Set<IToken> getAsyncRequests()
    {
        return asyncRequests;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     *
     * @param tasks EntityAITasks list to add tasks to.
     */
    public void addWorkerAIToTaskList(@NotNull final EntityAITasks tasks)
    {
        final AbstractAISkeleton tempAI = generateAI();

        if (tempAI == null)
        {
            Log.getLogger().error("Failed to create AI for citizen!");
            if (citizen == null)
            {
                Log.getLogger().error("CitizenData is null for job: " + nameTag + " jobClass: " + this.getClass());
                return;
            }
            Log.getLogger()
              .error(
                "Affected Citizen name:" + citizen.getName() + " id:" + citizen.getId() + " job:" + citizen.getJob() + " jobForAICreation:" + nameTag + " class:" + this.getClass()
                  + " entityPresent:"
                  + citizen.getCitizenEntity().isPresent());
            return;
        }

        workerAI = new WeakReference<>(tempAI);
        tasks.addTask(TASK_PRIORITY, tempAI);
    }

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
    public abstract AbstractAISkeleton<? extends AbstractJob> generateAI();

    /**
     * Check if the citizen already checked for food in his chest today.
     *
     * @return true if so.
     */
    public boolean hasCheckedForFoodToday()
    {
        return searchedForFoodToday;
    }

    /**
     * Sets that the citizen on this day already searched for food in his chest.
     */
    public void setCheckedForFood()
    {
        searchedForFoodToday = true;
    }

    /**
     * This method can be used to display the current status.
     * That a citizen is having.
     *
     * @return Small string to display info in name tag
     */
    public String getNameTagDescription()
    {
        return this.nameTag;
    }

    /**
     * Used by the AI skeleton to change a citizens name.
     * Mostly used to update debugging information.
     *
     * @param nameTag The name tag to display.
     */
    public final void setNameTag(final String nameTag)
    {
        this.nameTag = nameTag;
    }

    /**
     * Override this to let the worker return a bedTimeSound.
     *
     * @return soundEvent to be played.
     */
    public SoundEvent getBedTimeSound()
    {
        return null;
    }

    /**
     * Override this to let the worker return a badWeatherSound.
     *
     * @return soundEvent to be played.
     */
    public SoundEvent getBadWeatherSound()
    {
        return null;
    }

    /**
     * Override this to let the worker return a hostile move away sound.
     *
     * @return soundEvent to be played.
     */
    public SoundEvent getMoveAwaySound()
    {
        return null;
    }

    /**
     * Override this to implement Job specific death achievements.
     *
     * @param source  of the death
     * @param citizen which just died
     */
    public void triggerDeathAchievement(final DamageSource source, final EntityCitizen citizen)
    {

    }

    /**
     * Method called when a stack is pickup by the entity.
     *
     * @param pickedUpStack The stack that is being picked up.
     * @return true when the stack has been used to resolve a request, false when not.
     */
    public boolean onStackPickUp(@NotNull final ItemStack pickedUpStack)
    {
        if (getCitizen().getWorkBuilding() != null)
        {
            if (getCitizen().getWorkBuilding().overruleNextOpenRequestOfCitizenWithStack(getCitizen(), pickedUpStack.copy()))
            {
                return true;
            }
        }

        if (getCitizen().getHomeBuilding() != null)
        {
            if (getCitizen().getHomeBuilding().overruleNextOpenRequestOfCitizenWithStack(getCitizen(), pickedUpStack.copy()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Levelup actions on citizen levelup, allows custom actions based on Jobs
     */
    public void onLevelUp(final int newLevel)
    {
        // Default does nothing
    }

    /**
     * Get the CitizenData that this Job belongs to.
     *
     * @return CitizenData that owns this Job.
     */
    public CitizenData getCitizen()
    {
        return citizen;
    }

    /**
     * Executed every time the colony woke up.
     */
    public void onWakeUp()
    {
        searchedForFoodToday = false;
    }

    /**
     * Check if it is okay to eat
     *
     * @return true if so.
     */
    public boolean isOkayToEat()
    {
        return (workerAI.get() != null && workerAI.get().getState().isOkayToEat());
    }

    /**
     * Getter for the amount of actions done.
     *
     * @return the quantity.
     */
    public int getActionsDone()
    {
        return actionsDone;
    }

    /**
     * Actions done since the last reset.
     * Used for example to detect
     * if and when the inventory has to be dumped.
     */
    public void incrementActionsDone()
    {
        actionsDone++;
    }

    /**
     * Clear the actions done counter.
     * Call this when dumping into the chest.
     */
    public void clearActionsDone()
    {
        this.actionsDone = 0;
    }

    /**
     * Get the worker AI associated to this job
     *
     * @return worker AI
     */
    public AbstractAISkeleton getWorkerAI()
    {
        return workerAI.get();
    }

    /**
     * Check if the citizen is in an idle state.
     *
     * @return true if so.
     */
    public boolean isIdling()
    {
        return (workerAI.get() != null && workerAI.get().getState() == AIWorkerState.IDLE);
    }

    /**
     * Reset the AI after eating at a restaurant
     */
    public void resetAIAfterEating()
    {
        if (workerAI.get() != null)
        {
            workerAI.get().resetAI();
        }
    }
}
