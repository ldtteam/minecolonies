package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_JOB_TYPE;
import static com.minecolonies.api.util.constant.Suppression.CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION;

/**
 * Basic job information.
 * <p>
 * Suppressing Sonar Rule squid:S2390
 * This rule does "Classes should not access static members of their own subclasses during initialization"
 * But in this case the rule does not apply because
 * We are only mapping classes and that is reasonable
 */
@SuppressWarnings(CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION)
public abstract class AbstractJob<AI extends AbstractAISkeleton<J>, J extends AbstractJob<AI, J>> implements IJob<AI>
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
    private final ICitizenData citizen;

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
    private WeakReference<AI> workerAI = new WeakReference<>(null);

    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJob(final ICitizenData entity)
    {
        citizen = entity;
    }

    /**
     * Getter for the job which will be associated with the experience.
     *
     * @return the getName() or the specialized class name.
     */
    @Override
    public String getExperienceTag()
    {
        return getName();
    }

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    @Override
    public IModelType getModel()
    {
        return BipedModelType.CITIZEN;
    }

    /**
     * Get the Colony that this Job is associated with (shortcut for getAssignedCitizen().getColonyByPosFromWorld()).
     *
     * @return {@link Colony} of the citizen.
     */
    @Override
    public IColony getColony()
    {
        return citizen.getColony();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = new CompoundNBT();

        compound.putString(TAG_JOB_TYPE, getJobRegistryEntry().getRegistryName().toString());
        compound.put(TAG_ASYNC_REQUESTS, getAsyncRequests().stream().map(StandardFactoryController.getInstance()::serialize).collect(NBTUtils.toListNBT()));
        compound.putInt(TAG_ACTIONS_DONE, actionsDone);

        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        this.asyncRequests.clear();
        if (compound.keySet().contains(TAG_ASYNC_REQUESTS))
        {
            this.asyncRequests.addAll(NBTUtils.streamCompound(compound.getList(TAG_ASYNC_REQUESTS, Constants.NBT.TAG_COMPOUND))
                                        .map(StandardFactoryController.getInstance()::deserialize)
                                        .map(o -> (IToken) o)
                                        .collect(Collectors.toSet()));
        }
        if (compound.keySet().contains(TAG_ACTIONS_DONE))
        {
            actionsDone = compound.getInt(TAG_ACTIONS_DONE);
        }
    }

    /**
     * Save the Job to an CompoundNBT.
     *
     * @param compound CompoundNBT to save the Job to.
     */
    public void write(@NotNull final CompoundNBT compound)
    {

    }

    /**
     * Restore the Job from an CompoundNBT.
     *
     * @param compound CompoundNBT containing saved Job data.
     */
    public void read(@NotNull final CompoundNBT compound)
    {

    }

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    @Override
    public Set<IToken> getAsyncRequests()
    {
        return asyncRequests;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     *
     * @param tasks EntityAITasks list to add tasks to.
     */
    @Override
    public void addWorkerAIToTaskList(@NotNull final GoalSelector tasks)
    {
        final AI tempAI = generateAI();

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

        tasks.goals.stream().filter(goal -> goal.getGoal() instanceof AbstractAISkeleton).forEach(goal -> tasks.removeGoal(goal.getGoal()));
        workerAI = new WeakReference<>(tempAI);
        tasks.addGoal(TASK_PRIORITY, tempAI);
    }

    /**
     * Check if the citizen already checked for food in his chest today.
     *
     * @return true if so.
     */
    @Override
    public boolean hasCheckedForFoodToday()
    {
        return searchedForFoodToday;
    }

    /**
     * Sets that the citizen on this day already searched for food in his chest.
     */
    @Override
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
    @Override
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
    @Override
    public final void setNameTag(final String nameTag)
    {
        this.nameTag = nameTag;
    }

    /**
     * Override this to let the worker return a bedTimeSound.
     *
     * @return soundEvent to be played.
     */
    @Override
    public SoundEvent getBedTimeSound()
    {
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
        return null;
    }

    /**
     * Override this to let the worker return a hostile move away sound.
     *
     * @return soundEvent to be played.
     */
    @Override
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
    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {

    }

    /**
     * Method called when a stack is pickup by the entity.
     *
     * @param pickedUpStack The stack that is being picked up.
     * @return true when the stack has been used to resolve a request, false when not.
     */
    @Override
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
            return getCitizen().getHomeBuilding().overruleNextOpenRequestOfCitizenWithStack(getCitizen(), pickedUpStack.copy());
        }

        return false;
    }

    /**
     * Levelup actions on citizen levelup, allows custom actions based on Jobs
     */
    @Override
    public void onLevelUp(final int newLevel)
    {
        // Default does nothing
    }

    /**
     * Get the CitizenData that this Job belongs to.
     *
     * @return CitizenData that owns this Job.
     */
    @Override
    public ICitizenData getCitizen()
    {
        return citizen;
    }

    /**
     * Executed every time the colony woke up.
     */
    @Override
    public void onWakeUp()
    {
        searchedForFoodToday = false;
    }

    /**
     * Check if it is okay to eat
     *
     * @return true if so.
     */
    @Override
    public boolean isOkayToEat()
    {
        return (workerAI.get() != null && workerAI.get().getState().isOkayToEat());
    }

    /**
     * Getter for the amount of actions done.
     *
     * @return the quantity.
     */
    @Override
    public int getActionsDone()
    {
        return actionsDone;
    }

    /**
     * Actions done since the last reset.
     * Used for example to detect
     * if and when the inventory has to be dumped.
     */
    @Override
    public void incrementActionsDone()
    {
        actionsDone++;
    }

    /**
     * Clear the actions done counter.
     * Call this when dumping into the chest.
     */
    @Override
    public void clearActionsDone()
    {
        this.actionsDone = 0;
    }

    /**
     * Get the worker AI associated to this job
     *
     * @return worker AI
     */
    @Override
    public AI getWorkerAI()
    {
        return workerAI.get();
    }

    /**
     * Check if the citizen is in an idle state.
     *
     * @return true if so.
     */
    @Override
    public boolean isIdling()
    {
        return (workerAI.get() != null && workerAI.get().getState() == AIWorkerState.IDLE);
    }

    @Override
    public void resetAI()
    {
        if (workerAI.get() != null)
        {
            workerAI.get().resetAI();
        }
    }

    @Override
    public boolean allowsAvoidance()
    {
        return true;
    }

    @Override
    public int getDiseaseModifier()
    {
        return 1;
    }
}
