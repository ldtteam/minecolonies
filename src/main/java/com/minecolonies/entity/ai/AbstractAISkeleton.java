package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.state.AIStateBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.minecolonies.entity.EntityCitizen.Status.IDLE;

/**
 * Skeleton class for worker ai.
 * Here general target execution will be handled.
 * No utility on this level!
 * That's what {@link AbstractEntityAIWork} is for.
 *
 * @param <J> the job this ai will have
 */
public abstract class AbstractAISkeleton<J extends Job> extends EntityAIBase
{

    /**
     * Custom logger for the class.
     */
    private static final Logger log = Logger.getLogger(AbstractAISkeleton.class.getName());
    private static final int MUTEX_MASK = 3;
    protected final J job;
    protected final EntityCitizen worker;
    protected final World world;
    protected final ChatSpamFilter chatSpamFilter;
    private final ArrayList<AITarget> targetList;
    /**
     * The current state the ai is in.
     * Used to compare to state matching targets.
     */
    private AIStateBase state;

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
        this.state = AIStateBase.INIT;

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
    protected void registerTargets(AITarget... targets)
    {
        Arrays.asList(targets).forEach(this::registerTarget);
    }

    @Override
    public boolean shouldExecute()
    {
        return worker.getDesiredActivity() == EntityCitizen.DesiredActivity.WORK;
    }

    @Override
    public void resetTask()
    {
        worker.setStatus(IDLE);
    }

    @Override
    public void startExecuting()
    {
        worker.setStatus(EntityCitizen.Status.WORKING);
        log.info("Starting AI job " + job.getName());
    }

    @Override
    public void updateTask()
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
            log.log(Level.WARNING, "Condition check for target " + target + " threw an exception:", e);
            return false;
        }
        AIStateBase newState = null;
        try
        {
            newState = target.apply();
        }
        catch (Exception e)
        {
            log.log(Level.WARNING, "Action for target " + target + " threw an exception:", e);
            return false;
        }
        state = newState;
        return true;
    }

}
