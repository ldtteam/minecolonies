package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import com.minecolonies.entity.ai.util.ChatSpamFilter;
import com.minecolonies.util.Log;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

import static com.minecolonies.entity.EntityCitizen.Status.IDLE;

/**
 * Skeleton class for worker ai.
 * Here general target execution will be handled.
 * No utility on this level!
 * That's what {@link AbstractEntityAIInteract} is for.
 *
 * @param <J> the job this ai will have
 */
public abstract class AbstractAISkeleton<J extends Job> extends EntityAIBase
{

    private static final int    MUTEX_MASK = 3;
    protected final J                   job;
    protected final EntityCitizen       worker;
    protected final World               world;
    protected final ChatSpamFilter      chatSpamFilter;
    private final   ArrayList<AITarget> targetList;
    /**
     * The current state the ai is in.
     * Used to compare to state matching targets.
     */
    private         AIState             state;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractAISkeleton(final J job)
    {
        this.targetList = new ArrayList<>();
        setMutexBits(MUTEX_MASK);
        this.job = job;
        this.worker = this.job.getCitizen().getCitizenEntity();
        this.world = this.worker.worldObj;
        this.chatSpamFilter = new ChatSpamFilter(worker);
        this.state = AIState.INIT;

    }

    /**
     * Made final to preserve behaviour:
     * Sets a bitmask telling which other tasks may not run concurrently. The test is a simple bitwise AND - if it
     * yields zero, the two tasks may run concurrently, if not - they must run exclusively from each other.
     *
     * @param mutexBits the bits to flag this with.
     */
    @Override
    public final void setMutexBits(int mutexBits)
    {
        super.setMutexBits(mutexBits);
    }

    /**
     * Register one target.
     * @param target the target to register
     */
    private void registerTarget(AITarget target)
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
    protected final void registerTargets(AITarget... targets)
    {
        Arrays.asList(targets).forEach(this::registerTarget);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     *
     * @return true if execution is wanted
     */
    @Override
    public final boolean shouldExecute()
    {
        return worker.getDesiredActivity() == EntityCitizen.DesiredActivity.WORK;
    }

    /**
     * Resets the task
     */
    @Override
    public final void resetTask()
    {
        worker.setStatus(IDLE);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public final void startExecuting()
    {
        worker.setStatus(EntityCitizen.Status.WORKING);
        Log.logger.info("Starting AI job " + job.getName());
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public final boolean continueExecuting()
    {
        return super.continueExecuting();
    }

    /**
     * Updates the task
     */
    @Override
    public final void updateTask()
    {
        targetList.stream().anyMatch(this::checkOnTarget);
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
    private boolean checkOnTarget(AITarget target)
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
        catch (Exception e)
        {
            Log.logger.warn("Condition check for target " + target + " threw an exception:", e);
            return false;
        }
        return applyTarget(target);
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
    private boolean applyTarget(AITarget target)
    {
        AIState newState;
        try
        {
            newState = target.apply();
        }
        catch (Exception e)
        {
            Log.logger.warn("Action for target " + target + " threw an exception:", e);
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
     */
    public final AIState getState()
    {
        return state;
    }
}
