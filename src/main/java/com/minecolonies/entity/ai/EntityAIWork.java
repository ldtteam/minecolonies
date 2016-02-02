package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.colony.jobs.Job;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Utils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.entity.EntityCitizen.Status.IDLE;

public abstract class EntityAIWork<JOB extends Job> extends EntityAIBase
{
    private static final int DEFAULT_RANGE_FOR_DELAY = 3;
    private static Logger logger = Utils.generateLoggerForClass(EntityAIWork.class);
    protected final JOB job;
    protected final EntityCitizen worker;
    protected final World world;
    /**
     * A list of ItemStacks with needed items and their quantity.
     * This list is a diff between @see #itemsNeeded and
     * the players inventory and their hut combined.
     * So look here for what is currently still needed
     * to fulfill the workers needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    protected List<ItemStack> itemsCurrentlyNeeded;
    /**
     * The list of all items and their quantity that were requested by the worker.
     * Warning: This list does not change, if you need to see what is currently missing,
     * look at @see #itemsCurrentlyNeeded for things the miner needs.
     * <p>
     * Will be cleared on restart, be aware!
     */
    protected List<ItemStack> itemsNeeded;
    private ErrorState errorState;
    private ChunkCoordinates currentWorkingLocation;
    private int delay;
    private ChunkCoordinates currentStandingLocation;

    public EntityAIWork(JOB job)
    {
        setMutexBits(3);
        this.job = job;
        this.worker = this.job.getCitizen().getCitizenEntity();
        this.world = this.worker.worldObj;
        this.itemsNeeded = new ArrayList<>();
        this.itemsCurrentlyNeeded = new ArrayList<>();
        this.errorState = ErrorState.NONE;
        currentWorkingLocation = null;
        currentStandingLocation = null;
        delay = 0;
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
        //Something fatally wrong? Wait for re-init...
        if (null == getOwnBuilding())
        {
            //TODO: perhaps destroy this task? will see...
            return;
        }

        //Update torch, seeds etc. in chestbelt etc.
        updateRenderMetaData();

        //Wait for delay if it exists
        if (waitingForSomething())
        {
            return;
        }

        if (this.errorState == ErrorState.NEEDS_ITEM)
        {
            //TODO: request item
            return;
        }
        workOnTask();
    }

    /**
     * This method will be overridden by AI implementations.
     * It will serve as a tick function.
     */
    protected abstract void workOnTask();

    /**
     * Here the AI can check if the chestBelt has to be re rendered and do it.
     */
    protected void updateRenderMetaData(){}

    /**
     * Can be overridden in implementations to return the exact building type.
     *
     * @return the building associated with this AI's worker.
     */
    protected BuildingWorker getOwnBuilding()
    {
        return worker.getWorkBuilding();
    }

    private boolean waitingForSomething()
    {
        if (delay > 0)
        {
            if (!worker.isWorkerAtSiteWithMove(currentStandingLocation, DEFAULT_RANGE_FOR_DELAY))
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
    protected void clearWorkTarget()
    {
        this.currentStandingLocation = null;
        this.currentWorkingLocation = null;
        this.delay = 0;
        this.errorState = ErrorState.NONE;
    }

    /**
     * Sets the block the AI is currently walking to.
     *
     * @param stand where to walk to
     */
    protected boolean walkToBlock(ChunkCoordinates stand)
    {
        if(!Utils.isWorkerAtSite(worker,stand.posX,stand.posY,stand.posZ,DEFAULT_RANGE_FOR_DELAY))
        {
            workOnBlock(null, stand, 1);
            return true;
        }
        return false;
    }

    /**
     * Sets the block the AI is currently working on.
     * This block will receive animation hits on delay.
     *
     * @param target  the block that will be hit
     * @param stand   the block the worker will walk to
     * @param timeout the time in ticks to hit the block
     */
    protected void workOnBlock(ChunkCoordinates target, ChunkCoordinates stand, int timeout)
    {
        this.currentWorkingLocation = target;
        this.currentStandingLocation = stand;
        this.delay = timeout;
        this.errorState = ErrorState.WAITING;
    }

    private enum ErrorState
    {
        NONE,
        NEEDS_ITEM,
        WAITING,
    }

}
