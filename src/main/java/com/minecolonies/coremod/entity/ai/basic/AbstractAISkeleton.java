package com.minecolonies.coremod.entity.ai.basic;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static com.minecolonies.coremod.entity.EntityCitizen.Status.IDLE;

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

    private static final int MUTEX_MASK = 3;
    @NotNull
    protected final J                   job;
    @NotNull
    protected final EntityCitizen       worker;
    protected final World               world;
    @NotNull
    protected final ChatSpamFilter      chatSpamFilter;
    @NotNull
    private final   ArrayList<AITarget> targetList;
    /**
     * The current state the ai is in.
     * Used to compare to state matching targets.
     */
    private         AIState             state;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class.
     */
    protected AbstractAISkeleton(@NotNull final J job)
    {
        super();
        this.targetList = new ArrayList<>();
        setMutexBits(MUTEX_MASK);
        this.job = job;
        this.worker = this.job.getCitizen().getCitizenEntity();
        this.world = CompatibilityUtils.getWorld(this.worker);
        this.chatSpamFilter = new ChatSpamFilter(worker);
        this.state = AIState.INIT;
    }

    /**
     * Register one target.
     *
     * @param target the target to register.
     */
    private void registerTarget(final AITarget target)
    {
        targetList.add(target);
    }

    /**
     * Register all targets your ai needs.
     * They will be checked in the order of registration,
     * so sort them accordingly.
     *
     * @param targets a number of targets that need registration
     */
    protected final void registerTargets(final AITarget... targets)
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
        return worker.getDesiredActivity() == EntityCitizen.DesiredActivity.WORK;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public final boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    /**
     * Execute a one shot task or start executing a continuous task.
     */
    @Override
    public final void startExecuting()
    {
        worker.setStatus(EntityCitizen.Status.WORKING);
    }

    /**
     * Resets the task.
     */
    @Override
    public final void resetTask()
    {
        worker.setStatus(IDLE);
    }

    /**
     * Updates the task.
     */
    @Override
    public final void updateTask()
    {
        targetList.stream().anyMatch(this::checkOnTarget);
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
     * Checks on one target to see if it has to be executed.
     * It first checks for the state of the ai.
     * If that matches it tests the predicate if the ai
     * wants to run the target.
     * And if that's a yes, runs the target.
     * Tester and target are both error-checked
     * to prevent minecraft from crashing on bad ai.
     *
     * @param target the target to check
     * @return true if this target worked and we should stop executing this tick
     */
    private boolean checkOnTarget(@NotNull final AITarget target)
    {
        if (state != target.getState() && target.getState() != null)
        {
            return false;
        }
        try
        {
            if (!target.test())
            {
                return false;
            }
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Condition check for target " + target + " threw an exception:", e);
            this.onException(e);
            return false;
        }
        return applyTarget(target);
    }

    /**
     * Handle an exception higher up.
     *
     * @param e The exception to be handled.
     */
    protected void onException(final RuntimeException e)
    {
    }

    /**
     * Continuation of checkOnTarget.
     * applies the target and changes the state.
     * if the state is null, execute more targets
     * and don't change state.
     *
     * @param target the target.
     * @return true if it worked.
     */
    private boolean applyTarget(@NotNull final AITarget target)
    {
        final AIState newState;
        try
        {
            newState = target.apply();
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Action for target " + target + " threw an exception:", e);
            this.onException(e);
            return false;
        }
        if (newState != null)
        {
            state = newState;
            return true;
        }
        return false;
    }

    /**
     * Get the current state the ai is in.
     *
     * @return The current AIState.
     */
    public final AIState getState()
    {
        return state;
    }

    protected int getLevelDelay()
    {
        return 10;
    }
}
