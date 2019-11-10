package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.ai.util.ChatProxy;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;

/**
 * Skeleton class for worker ai.
 * Here general target execution will be handled.
 * No utility on this level!
 *
 * @param <J> the job this ai will have.
 */
public abstract class AbstractAISkeleton<J extends IJob> extends Goal
{

    private static final Flag                  MUTEX_MASK =  Flag.MOVE;
    @NotNull
    protected final      J                     job;
    @NotNull
    protected final      AbstractEntityCitizen worker;
    protected final      World                 world;
    @NotNull
    protected final      ChatProxy             chatProxy;

    /**
     * The statemachine this AI uses
     */
    @NotNull
    private final ITickRateStateMachine stateMachine;

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

        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
        this.job = job;
        this.worker = this.job.getCitizen().getCitizenEntity().get();
        this.world = CompatibilityUtils.getWorldFromCitizen(this.worker);
        this.chatProxy = new ChatProxy(job.getCitizen());
        stateMachine = new TickRateStateMachine(AIWorkerState.INIT, this::onException);

        // Start at a random tickcounter to spread AI updates over all ticks
        tickCounter = new Random().nextInt(MineColonies.getConfig().getCommon().updateRate.get()) + 1;
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
     * Returns whether the Goal should begin execution.
     *
     * @return true if execution is wanted.
     */
    @Override
    public final boolean shouldExecute()
    {
        return worker.getDesiredActivity() == DesiredActivity.WORK;
    }

    /**
     * Returns whether an in-progress Goal should continue executing.
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
    public final void tick()
    {
        if (tickCounter < MineColonies.getConfig().getCommon().updateRate.get())
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
    public final void setMutexFlags(final EnumSet<Flag> mutexBits)
    {
        super.setMutexFlags(mutexBits);
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
