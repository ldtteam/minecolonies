package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;

/**
 * Skeleton class for worker ai.
 * Here general target execution will be handled.
 * No utility on this level!
 * That's what {@link AbstractEntityAIInteract} is for.
 *
 * @param <J> the job this ai will have.
 */
public abstract class AbstractAISkeleton<J extends AbstractJob> extends EntityAIBase
{

    private static final int            MUTEX_MASK = 3;
    @NotNull
    protected final      J              job;
    @NotNull
    protected final      EntityCitizen  worker;
    protected final      World          world;
    @NotNull
    protected final      ChatSpamFilter chatSpamFilter;

    /**
     * The statemachine this AI uses
     */
    @NotNull
    private final TickRateStateMachine stateMachine;

    /**
     * Counter for updateTask ticks received
     */
    private int tickCounter = 0;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    protected AbstractAISkeleton(@NotNull final J job)
    {
        super();

        if (!job.getCitizen().getCitizenEntity().isPresent())
        {
            throw new IllegalArgumentException("Cannot instantiate a AI from a Job that is attached to a Citizen without entity.");
        }

        setMutexBits(MUTEX_MASK);
        this.job = job;
        this.worker = this.job.getCitizen().getCitizenEntity().get();
        this.world = CompatibilityUtils.getWorld(this.worker);
        this.chatSpamFilter = new ChatSpamFilter(job.getCitizen());
        stateMachine = new TickRateStateMachine(AIWorkerState.INIT, this::onException);

        // Start at a random tickcounter to spread AI updates over all ticks
        tickCounter = new Random().nextInt(Configurations.gameplay.updateRate) + 1;
    }

    /**
     * Register one target.
     *
     * @param target the target to register.
     */
    protected void registerTarget(final TickingTransition target)
    {
        stateMachine.addTransition(target);
    }

    /**
     * Register all targets your ai needs.
     * They will be checked in the order of registration,
     * so sort them accordingly.
     *
     * @param targets a number of targets that need registration
     */
    protected final void registerTargets(final TickingTransition... targets)
    {
        Arrays.asList(targets).forEach(this::registerTarget);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     *
     * @return true if execution is wanted.
     */
    @Override
    public final boolean shouldExecute()
    {
        return worker.getDesiredActivity() == DesiredActivity.WORK;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public final boolean shouldContinueExecuting()
    {
        return super.shouldContinueExecuting();
    }

    /**
     * Execute a one shot task or start executing a continuous task.
     */
    @Override
    public final void startExecuting()
    {
        worker.getCitizenStatusHandler().setStatus(Status.WORKING);
    }

    /**
     * Resets the task.
     */
    @Override
    public final void resetTask()
    {
        worker.getCitizenStatusHandler().setStatus(Status.IDLE);
    }

    /**
     * Updates the task.
     */
    @Override
    public final void updateTask()
    {
        if (tickCounter < Configurations.gameplay.updateRate)
        {
            tickCounter++;
        }
        else
        {
            stateMachine.tick();
            tickCounter = 1;
        }
    }

    protected void onException(final RuntimeException e)
    {
    }

    /**
     * Made final to preserve behaviour:
     * Sets a bitmask telling which other tasks may not run concurrently. The test is a simple bitwise AND - if it
     * yields zero, the two tasks may run concurrently, if not - they must run exclusively from each other.
     *
     * @param mutexBits the bits to flag this with.
     */
    @Override
    public final void setMutexBits(final int mutexBits)
    {
        super.setMutexBits(mutexBits);
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
     * Resets the worker AI to Idle state, use with care interrupts all current Actions
     */
    public void resetAI()
    {
        stateMachine.addTransition(new AIOneTimeEventTarget(AIWorkerState.IDLE));
    }
}
