package com.minecolonies.entity.ai.basic;

import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.entity.ai.util.AIState.IDLE;
import static com.minecolonies.entity.ai.util.AIState.INIT;

/**
 * This class provides basic ai functionality.
 */
public abstract class AbstractEntityAIBasic<J extends Job> extends AbstractAISkeleton<J>
{

    /**
     * The default range for any walking to blocks.
     */
    protected static final int      DEFAULT_RANGE_FOR_DELAY = 3;
    /**
     * The block the ai is currently working at or wants to work.
     */
    protected              BlockPos currentWorkingLocation  = null;
    /**
     * The time in ticks until the next action is made
     */
    protected              int      delay                   = 0;
    /**
     * The block the ai is currently standing at or wants to stand.
     */
    protected              BlockPos currentStandingLocation = null;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAIBasic(J job)
    {
        super(job);
        super.registerTargets(
                /**
                 * Init safety checks and transition to IDLE
                 */
                new AITarget(INIT, this::initSafetyChecks),
                /**
                 * Update chestbelt and nametag
                 * Will be executed every time
                 * and does not stop execution
                 */
                new AITarget(this::updateVisualState),
                /**
                 * If waitingForSomething returns true
                 * stop execution to wait for it.
                 * this keeps the current state
                 * (returning null would not stop execution)
                 */
                new AITarget(this::waitingForSomething, this::getState)
                             );
    }

    /**
     * Check for null on important variables to prevent crashes.
     *
     * @return IDLE if all ready, else stay in INIT
     */
    private AIState initSafetyChecks()
    {
        //Something fatally wrong? Wait for re-init...
        if (null == getOwnBuilding())
        {
            //TODO: perhaps destroy this task? will see...
            return INIT;
        }
        return IDLE;
    }

    /**
     * Can be overridden in implementations to return the exact building type.
     *
     * @return the building associated with this AI's worker.
     */
    @Nullable
    protected BuildingWorker getOwnBuilding()
    {
        return worker.getWorkBuilding();
    }

    /**
     * Updates the visual state of the worker.
     * Updates render meta data.
     * Updates the current state on the nametag.
     *
     * @return null to execute more targets.
     */
    private AIState updateVisualState()
    {
        //Update the current state the worker is in.
        job.setNameTag(this.getState().toString());
        //Update torch, seeds etc. in chestbelt etc.
        updateRenderMetaData();
        return null;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the chestBelt has to be re rendered and do it.
     */
    protected void updateRenderMetaData()
    {
        worker.setRenderMetadata("");
    }

    /**
     * This method will return true if the AI is waiting for something.
     * In that case, don't execute any more AI code, until it returns false.
     * Call this exactly once per tick to get the delay right.
     * The worker will move and animate correctly while he waits.
     *
     * @return true if we have to wait for something
     * @see #currentStandingLocation @see #currentWorkingLocation
     * @see #DEFAULT_RANGE_FOR_DELAY @see #delay
     */
    private boolean waitingForSomething()
    {
        if (delay > 0)
        {
            if (currentStandingLocation != null &&
                !worker.isWorkerAtSiteWithMove(currentStandingLocation, DEFAULT_RANGE_FOR_DELAY))
            {
                //Don't decrease delay as we are just walking...
                return true;
            }
            worker.hitBlockWithToolInHand(currentWorkingLocation);
            delay--;
            return true;
        }
        clearWorkTarget();
        return false;
    }

    /**
     * Remove the current working block and it's delay.
     */
    private void clearWorkTarget()
    {
        this.currentStandingLocation = null;
        this.currentWorkingLocation = null;
        this.delay = 0;
    }
}
