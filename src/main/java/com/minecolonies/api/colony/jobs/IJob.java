package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.IAssignsJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.ai.ITickingStateAI;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.minecolonies.api.util.constant.HappinessConstants.IDLE_AT_JOB_COMPLAINS_DAYS;
import static com.minecolonies.api.util.constant.HappinessConstants.IDLE_AT_JOB_DEMANDS_DAYS;

public interface IJob<AI extends ITickingStateAI> extends INBTSerializable<CompoundTag>
{
    /**
     * The {@link JobEntry} for this job.
     *
     * @return The {@link JobEntry}.
     */
    JobEntry getJobRegistryEntry();

    /**
     * Get the RenderBipedCitizen.Model to use when the Citizen performs this job role.
     *
     * @return Model of the citizen.
     */
    ResourceLocation getModel();

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
    Set<IToken<?>> getAsyncRequests();

    /**
     * Creates the work AI
     */
    void createAI();

    /**
     * Generate your AI class to register.
     * <p>
     * Suppressing Sonar Rule squid:S1452 This rule does "Generic wildcard types should not be used in return parameters" But in this case the rule does not apply because We are
     * fine with all AbstractJob implementations and need generics only for java
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
     * This method can be used to display the current status. That a citizen is having.
     *
     * @return Small string to display info in name tag
     */
    String getNameTagDescription();

    /**
     * Used by the AI skeleton to change a citizens name. Mostly used to update debugging information.
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
    default void onLevelUp()
    {}

    /**
     * Initizalizes values for an entity when the entity is spawned/assigned to the job
     *
     * @param citizen
     */
    default void initEntityValues(AbstractEntityCitizen citizen)
    {
        citizen.getCitizenData().setIdleAtJob(false);
    }

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
    boolean canAIBeInterrupted();

    /**
     * Getter for the amount of actions done.
     *
     * @return the quantity.
     */
    int getActionsDone();

    /**
     * Increase the actions done since the last reset by 1 Used for example to detect if and when the inventory has to be dumped.
     */
    void incrementActionsDone();

    /**
     * Increase the actions done since the last reset by numberOfActions Used for example to detect if and when the inventory has to be dumped.
     */
    void incrementActionsDone(int numberOfActions);

    /**
     * Clear the actions done counter. Call this when dumping into the chest.
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
    double getDiseaseModifier();

    /**
     * When job removed (death of citizen or job change).
     */
    void onRemoval();

    /**
     * Check if the particular job ignores a particular damage type.
     *
     * @param damageSource the damage source to check.
     * @return true if so.
     */
    boolean ignoresDamage(@NotNull final DamageSource damageSource);

    /**
     * Mark a request as a synchronous (blocking request).
     *
     * @param id the id.
     */
    void markRequestSync(IToken<?> id);

    /**
     * If the worker can pick up the stack.
     * @param pickedUpStack the stack to check.
     * @return true if so.
     */
    boolean pickupSuccess(@NotNull ItemStack pickedUpStack);

    /**
     * Process time the colony was offline.
     * @param time the time in seconds.
     */
    void processOfflineTime(long time);

    /**
     * Serialize the job to a buffer.
     * @param buffer the buffer to serialize it to.
     */
    void serializeToView(final FriendlyByteBuf buffer);

    /**
     * Get the time limit in seconds after which the job considers itself inactive.
     * @return the limit, or -1 if not applicable.
     */
    default int getInactivityLimit()
    {
        return -1;
    }

    /**
     * Get the days before complaining or demanding solution for being idle
     * @param isDemand true if looking for the demand time
     * @return number of days
     */
    default int getIdleSeverity(boolean isDemand)
    {
        if(isDemand)
        {
            return IDLE_AT_JOB_DEMANDS_DAYS;
        }
        else
        {
            return IDLE_AT_JOB_COMPLAINS_DAYS;
        }
    }

    /**
     * Trigger a job based action on activity change (active to inactive, or inactive to active).
     * @param newState the new state (true for active, false for inactive).
     */
    default void triggerActivityChangeAction(boolean newState)
    {
        //noop.
    }

    /**
     * Set the registry entry of the job.
     *
     * @param jobEntry the job entry belonging to it.
     */
    void setRegistryEntry(JobEntry jobEntry);

    /**
     * Whether the job is a guard
     *
     * @return
     */
    default boolean isGuard()
    {
        return false;
    }

    /**
     * Gets the position of the building the job is assigned to
     * @return
     */
    public BlockPos getBuildingPos();

    /**
     * Gets the work building
     * @return
     */
    public IBuilding getWorkBuilding();

    /**
     * Gets the module this job is assigned to
     */
    IAssignsJob getWorkModule();

    /**
     * Assigns the job to its specific work module
     * @param module
     * @return
     */
    boolean assignTo(IAssignsJob module);
}
