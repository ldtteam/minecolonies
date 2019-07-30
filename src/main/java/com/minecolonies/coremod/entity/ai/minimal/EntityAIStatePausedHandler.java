package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.IEntityCitizen;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;

/**
 * Class for handling AI in paused state
 */
public final class EntityAIStatePausedHandler
{
    /**
     * The worker which is paused
     */
    private final IEntityCitizen   worker;
    private final IBuildingWorker building;

    /**
     * Wander AI task if worker paused
     */
    private EntityAICitizenWander         wander;
    private static final double           RANDOM_MODIFIER = 1.0D / 40.0D;

    /**
     * Create a new state paused handler
     * 
     * @param w paused worker
     * @param b his building
     */
    public EntityAIStatePausedHandler(final IEntityCitizen w, final IBuildingWorker b)
    {
        this.worker = w;
        this.building = b;
    }

    /**
     * Proceed pausing <br>
     * If walking keeps walking <br>
     * Else pick a new activity
     */
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition","PMD.EmptyIfStmt"})
    public void doPause()
    {
        // Jump out if walking.
        if (wander != null && wander.shouldContinueExecuting())
        {
            return;
        }

        // Pick random activity. TODO: add also random near building picker
        final int percent = worker.getRNG().nextInt(100);
        if(percent < 8)
        {
            goCheckOwnWorkerBuilding();
        }
        else if (percent < 35)
        {
            wanderAround();
        }
        else
        {
            // Stand and stare.
        }
    }

    /**
     * I should look at the work of others.
     */
    private void wanderAround()
    {
        wander = new EntityAICitizenWander(worker, DEFAULT_SPEED, RANDOM_MODIFIER);
        if (wander.shouldExecute())
        {
            wander.startExecuting();
        }
    }

    /**
     * Somebody is tricking my chest!
     */
    private void goCheckOwnWorkerBuilding()
    {
        wander = new EntityAICitizenCheckWorkerBuilding(worker, (worker.getRNG().nextBoolean()) ? DEFAULT_SPEED * 1.5D : DEFAULT_SPEED * 2.2D, building, RANDOM_MODIFIER);
        if (wander.shouldExecute())
        {
            wander.startExecuting();
        }
    }
}