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
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
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
    private static final int TASK_PRIORITY = 4;

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

    @Override
    public String getExperienceTag()
    {
        return getName();
    }

    @Override
    public IModelType getModel()
    {
        return BipedModelType.CITIZEN;
    }

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
        compound.put(TAG_ASYNC_REQUESTS,
          getAsyncRequests().stream()
            .filter(token -> getColony().getRequestManager().getRequestForToken(token) != null)
            .map(StandardFactoryController.getInstance()::serialize)
            .collect(NBTUtils.toListNBT()));
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

    @Override
    public Set<IToken> getAsyncRequests()
    {
        return asyncRequests;
    }

    @Override
    public void addWorkerAIToTaskList(@NotNull final GoalSelector tasks)
    {
        final AI tempAI = generateAI();

        if (tempAI == null)
        {
            Log.getLogger().error("Failed to create AI for citizen!", new Exception());
            if (citizen == null)
            {
                Log.getLogger().error("CitizenData is null for job: " + nameTag + " jobClass: " + this.getClass(), new Exception());
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

    @Override
    public boolean hasCheckedForFoodToday()
    {
        return searchedForFoodToday;
    }

    @Override
    public void setCheckedForFood()
    {
        searchedForFoodToday = true;
    }

    @Override
    public String getNameTagDescription()
    {
        return this.nameTag;
    }

    @Override
    public final void setNameTag(final String nameTag)
    {
        this.nameTag = nameTag;
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final AbstractEntityCitizen citizen)
    {

    }

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

    @Override
    public void onLevelUp()
    {

    }

    @Override
    public ICitizenData getCitizen()
    {
        return citizen;
    }

    @Override
    public void onWakeUp()
    {
        searchedForFoodToday = false;
    }

    @Override
    public boolean isOkayToEat()
    {
        return (workerAI.get() != null && workerAI.get().getState().isOkayToEat());
    }

    @Override
    public int getActionsDone()
    {
        return actionsDone;
    }

    @Override
    public void incrementActionsDone()
    {
        actionsDone++;
    }

    @Override
    public void incrementActionsDone(final int numberOfActions)
    {
        actionsDone += numberOfActions;
    }

    @Override
    public void clearActionsDone()
    {
        this.actionsDone = 0;
    }

    @Override
    public AI getWorkerAI()
    {
        return workerAI.get();
    }

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

    @Override
    public void onRemoval()
    {

    }

    @Override
    public void setActive(final boolean b)
    {

    }
}
