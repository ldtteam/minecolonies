package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.AIState;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.coremod.entity.ai.util.AIState.PAUSED;

/**
 * Static class for handling AI in paused state
 */
public final class EntityAIStatePausedHandler
{
    private static Random random = new Random();

    /**
     * The worker which is paused
     */
    private static EntityCitizen worker;
    private static AbstractBuildingWorker building;

    /**
     * Wander AI task if worker paused
     */
    private static EntityAICitizenWander wander;

    /**
     * Private constructor to hide the implicit public one.
     */
    private EntityAIStatePausedHandler()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Class constructor
     */
    public static AIState doPause(final EntityCitizen w, final AbstractBuildingWorker b)
    {
        worker = w;
        building = b;
        wander = new EntityAICitizenWander(worker, DEFAULT_SPEED);

        // Pick random activity
        int percent = random.nextInt(100);
        if(percent < 15)
        {
            goCheckOwnWorkerBuilding();
        }
        else if (percent < 35)
        {
            wanderAround();
        }
        else
        {
            // Stand and stare
        }
        Log.getLogger().info(percent);

        return PAUSED;
    }

    /**
     * I should look at the work of others. TODO: add also random near building picker
     */
    private static void wanderAround()
    {
        if (!wander.shouldContinueExecuting())
        {
            wander = new EntityAICitizenWander(worker, DEFAULT_SPEED);
            if (wander.shouldExecute())
            {
                wander.startExecuting();
            }
        }
    }

    /**
     * Somebody is tricking my chest!
     */
    private static void goCheckOwnWorkerBuilding()
    {
        if (!wander.shouldContinueExecuting())
        {
            wander = new EntityAICitizenCheckWorkerBuilding(worker, (random.nextBoolean()) ? DEFAULT_SPEED * 2.0D : DEFAULT_SPEED * 3.0D, building);
            if (wander.shouldExecute())
            {
                wander.startExecuting();
            }
        }
    }

    private static class EntityAICitizenCheckWorkerBuilding extends EntityAICitizenWander
    {
        private static AbstractBuildingWorker building;

        private EntityAICitizenCheckWorkerBuilding(final EntityCitizen citizen, final double speed, final AbstractBuildingWorker b)
        {
            super(citizen, speed);
            building = b;
        }
        
        @Override
        public void startExecuting()
        {
            super.citizen.getNavigator().tryMoveToBlockPos(building.getLocation(), super.speed);
        }
    }
}