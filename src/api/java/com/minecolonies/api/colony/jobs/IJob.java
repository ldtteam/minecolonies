package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IJob<AI extends Goal> extends INBTSerializable<CompoundNBT>
{

    /**
     * The {@link JobEntry} for this job.
     *
     * @return The {@link JobEntry}.
     */
    JobEntry getJobRegistryEntry();

    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    String getName();

    /**
     * Getter for the job which will be associated with the experience.
     *
     * @return the getName() or the specialized class name.
     */
    String getExperienceTag();

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    IModelType getModel();

    /**
     * Get the Colony that this Job is associated with (shortcut for getAssignedCitizen().getColonyByPosFromWorld()).
     *
     * @return {@link com.minecolonies.api.colony.IColony} of the citizen.
     */
    IColony getColony();

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    Set<IToken> getAsyncRequests();

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     *
     * @param tasks EntityAITasks list to add tasks to.
     */
    void addWorkerAIToTaskList(@NotNull GoalSelector tasks);

    /**
     * Generate your AI class to register.
     * <p>
     * Suppressing Sonar Rule squid:S1452 This rule does "Generic wildcard types should not be used in return parameters"
     * But in this case the rule does not apply because
     * We are fine with all AbstractJob implementations and need generics only for java
     *
     * @return your personal AI instance.
     */
    @SuppressWarnings("squid:S1452")
    AI generateAI();

    /**
     * Check if the citizen already checked for food in his chest today.
     *
     * @return true if so.
     */
    boolean hasCheckedForFoodToday();

    /**
     * Sets that the citizen on this day already searched for food in his chest.
     */
    void setCheckedForFood();

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
     * Override this to implement Job specific death achievements.
     *
     * @param source  of the death
     * @param citizen which just died
     */
    void triggerDeathAchievement(DamageSource source, AbstractEntityCitizen citizen);

    /**
     * Method called when a stack is pickup by the entity.
     *
     * @param pickedUpStack The stack that is being picked up.
     * @return true when the stack has been used to resolve a request, false when not.
     */
    boolean onStackPickUp(@NotNull ItemStack pickedUpStack);

    /**
     * Levelup actions on citizen levelup, allows custom actions based on Jobs
     */
    void onLevelUp();

    /**
     * Get the CitizenData that this Job belongs to.
     *
     * @return CitizenData that owns this Job.
     */
    ICitizenData getCitizen();

    /**
     * Executed every time the colony woke up.
     */
    void onWakeUp();

    /**
     * Check if it is okay to eat
     *
     * @return true if so.
     */
    boolean isOkayToEat();

    /**
     * Getter for the amount of actions done.
     *
     * @return the quantity.
     */
    int getActionsDone();

    /**
     * Increase the actions done since the last reset by 1
     * Used for example to detect if and when the inventory has to be dumped.
     */
    void incrementActionsDone();

    /**
     * Increase the actions done since the last reset by numberOfActions
     * Used for example to detect if and when the inventory has to be dumped.
     */
    void incrementActionsDone(int numberOfActions);

    /**
     * Clear the actions done counter.
     * Call this when dumping into the chest.
     */
    void clearActionsDone();

    /**
     * Get the worker AI associated to this job
     *
     * @return worker AI
     */
    AI getWorkerAI();

    /**
     * Check if the citizen is in an idle state.
     *
     * @return true if so.
     */
    boolean isIdling();

    /**
     * Reset the AI.
     */
    void resetAI();

    /**
     * Method to check if the colony job allows avoidance.
     *
     * @return true if so.
     */
    boolean allowsAvoidance();

    /**
     * Disease modifier of the job.
     *
     * @return the modifier of the job.
     */
    int getDiseaseModifier();

    /**
     * When job removed (death of citizen or job change).
     */
    void onRemoval();

    /**
     * Set if the worker can currently work.
     *
     * @param b true if so.
     */
    void setActive(final boolean b);

    /**
     * Check if the particular job ignores a particular damage type.
     * @param damageSource the damage source to check.
     * @return true if so.
     */
    boolean ignoresDamage(@NotNull final DamageSource damageSource);
}
