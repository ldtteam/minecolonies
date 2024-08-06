package com.minecolonies.core.entity.ai.workers;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.ITickingStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.ENTITY_AI_TICKRATE;

/**
 * Skeleton class for worker ai. Here general target execution will be handled. No utility on this level!
 *
 * @param <J> the job this ai will have.
 */
public abstract class AbstractAISkeleton<J extends IJob<?>> implements ITickingStateAI
{
    @NotNull
    protected final J                     job;
    @NotNull
    protected final AbstractEntityCitizen worker;
    protected final ServerLevel           world;

    /**
     * The statemachine this AI uses
     */
    @NotNull
    private final ITickRateStateMachine<IAIState> stateMachine;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    protected AbstractAISkeleton(@NotNull final J job)
    {
        super();

        if (!job.getCitizen().getEntity().isPresent())
        {
            throw new IllegalArgumentException("Cannot instantiate a AI from a Job that is attached to a Citizen without entity.");
        }

        this.job = job;
        this.worker = this.job.getCitizen().getEntity().get();
        this.world = (ServerLevel) CompatibilityUtils.getWorldFromCitizen(this.worker);
        stateMachine = new TickRateStateMachine<>(AIWorkerState.INIT, this::onException, ENTITY_AI_TICKRATE);
    }

    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    /**
     * Register one target.
     *
     * @param target the target to register.
     */
    public void registerTarget(final TickingTransition<IAIState> target)
    {
        stateMachine.addTransition(target);
    }

    /**
     * Register all targets your ai needs. They will be checked in the order of registration, so sort them accordingly.
     *
     * @param targets a number of targets that need registration
     */
    protected final void registerTargets(final TickingTransition<IAIState>... targets)
    {
        Arrays.asList(targets).forEach(this::registerTarget);
    }

    protected void onException(final RuntimeException e)
    {
    }

    /**
     * Get the current state the ai is in.
     *
     * @return The current IAIState.
     */
    public final IAIState getState()
    {
        return stateMachine.getState();
    }

    /**
     * Gets the update rate of the worker's statemachine
     *
     * @return update rate
     */
    public int getTickRate()
    {
        return stateMachine.getTickRate();
    }

    /**
     * Whether the AI is allowed to be interrupted
     *
     * @return true if can be interrupted
     */
    public boolean canBeInterrupted()
    {
        return getState().isOkayToEat();
    }

    /**
     * Resets the worker AI to Idle state, use with care interrupts all current Actions
     */
    public void resetAI()
    {
        stateMachine.reset();
        worker.setRenderMetadata("");
    }

    /**
     * Get the statemachine of the AI
     *
     * @return statemachine
     */
    public ITickRateStateMachine<IAIState> getStateAI()
    {
        return stateMachine;
    }

    /**
     * On removal of the AI.
     * Clean up equipment.
     */
    public void onRemoval()
    {
        worker.setItemSlot(EquipmentSlot.CHEST, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.FEET, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.HEAD, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.LEGS, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.OFFHAND, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);

        worker.getInventoryCitizen().moveArmorToInventory(EquipmentSlot.CHEST);
        worker.getInventoryCitizen().moveArmorToInventory(EquipmentSlot.FEET);
        worker.getInventoryCitizen().moveArmorToInventory(EquipmentSlot.HEAD);
        worker.getInventoryCitizen().moveArmorToInventory(EquipmentSlot.LEGS);
    }

    /**
     * Sets the delay to next execution for the currently executed transition
     *
     * @param ticksToNextUpdate
     */
    public void setCurrentDelay(final int ticksToNextUpdate)
    {
        stateMachine.setCurrentDelay(ticksToNextUpdate);
    }
}
