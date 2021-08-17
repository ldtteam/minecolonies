package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Skeleton class for worker ai. Here general target execution will be handled. No utility on this level!
 *
 * @param <J> the job this ai will have.
 */
public abstract class AbstractAISkeleton<J extends IJob<?>> extends Goal
{
    @NotNull
    protected final J                     job;
    @NotNull
    protected final AbstractEntityCitizen worker;
    protected final World                 world;

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

        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.job = job;
        this.worker = this.job.getCitizen().getEntity().get();
        this.world = CompatibilityUtils.getWorldFromCitizen(this.worker);
        stateMachine = new TickRateStateMachine<>(AIWorkerState.INIT, this::onException);
        stateMachine.setTickRate(MineColonies.getConfig().getServer().updateRate.get());
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

    /**
     * Returns whether the Goal should begin execution.
     *
     * @return true if execution is wanted.
     */
    @Override
    public final boolean canUse()
    {
        return worker.getDesiredActivity() == DesiredActivity.WORK;
    }

    /**
     * Returns whether an in-progress Goal should continue executing.
     */
    @Override
    public final boolean canContinueToUse()
    {
        return super.canContinueToUse();
    }

    /**
     * Execute a one shot task or start executing a continuous task.
     */
    @Override
    public final void start()
    {
        worker.getCitizenStatusHandler().setStatus(Status.WORKING);
        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
    }

    /**
     * Resets the task.
     */
    @Override
    public final void stop()
    {
        resetAI();
        worker.getCitizenData().setVisibleStatus(null);
    }

    /**
     * Updates the task.
     */
    @Override
    public final void tick()
    {
        stateMachine.tick();
    }

    protected void onException(final RuntimeException e)
    {
    }

    /**
     * Made final to preserve behaviour: Sets a bitmask telling which other tasks may not run concurrently. The test is a simple bitwise AND - if it yields zero, the two tasks may
     * run concurrently, if not - they must run exclusively from each other.
     *
     * @param mutexBits the bits to flag this with.
     */
    @Override
    public final void setFlags(final EnumSet<Flag> mutexBits)
    {
        super.setFlags(mutexBits);
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
        worker.setItemSlot(EquipmentSlotType.CHEST, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlotType.FEET, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlotType.HEAD, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlotType.LEGS, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlotType.OFFHAND, ItemStackUtils.EMPTY);
        worker.setItemSlot(EquipmentSlotType.MAINHAND, ItemStackUtils.EMPTY);
    }
}
