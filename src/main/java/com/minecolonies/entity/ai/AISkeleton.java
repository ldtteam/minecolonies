package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.state.AIStateBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import static com.blockout.Loader.logger;
import static com.minecolonies.entity.EntityCitizen.Status.IDLE;

/**
 * Skeleton class for Worker AI
 */
public abstract class AISkeleton<J extends Job> extends EntityAIBase
{

    /**
     * Custom logger for the class.
     */
    private static final Logger LOGGER = Logger.getLogger(AISkeleton.class.getName());
    private static final int MUTEX_MASK = 3;
    protected final J job;
    protected final EntityCitizen worker;
    protected final World world;
    protected final ChatSpamFilter chatSpamFilter;
    private final ArrayList<AITarget> targetList;
    private AIStateBase state;


    protected AISkeleton(final J job)
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
        logger.info("Starting AI job " + job.getName());
    }

    @Override
    public void updateTask()
    {
        targetList.stream().anyMatch(this::checkOnTarget);
    }

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
            logger.warn("Condition check for target " + target + " threw an exception:", e);
            return false;
        }
        AIStateBase newState = null;
        try
        {
            newState = target.apply();
        }
        catch (Exception e)
        {
            logger.warn("Action for target " + target + " threw an exception:", e);
            return false;
        }
        state = newState;
        return true;
    }

}
