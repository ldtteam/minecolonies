package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;

/**
 * Static class for handling AI in paused state
 */
public final class EntityAIStatePausedHandler
{
    private static Random                 random = new Random();

    /**
     * The worker which is paused
     */
    private static EntityCitizen          worker;
    private static AbstractBuildingWorker building;

    /**
     * Wander AI task if worker paused
     */
    private static EntityAICitizenWander  wander;
    private static final double           RANDOM_MODIFIER = 1.0D / 30.0D;

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
     * 
     * @param w paused worker
     * @param b his building
     */
    public static void doPause(final EntityCitizen w, final AbstractBuildingWorker b)
    {
        worker = w;
        building = b;
        wander = new EntityAICitizenWander(worker, DEFAULT_SPEED, RANDOM_MODIFIER);

        // Jump out if walking.
        if (wander.shouldContinueExecuting())
        {
            return;
        }

        // Pick random activity.
        int percent = random.nextInt(100);
        if(percent < 8)
        {
            goCheckOwnWorkerBuilding();
        }
        else if (percent < 40)
        {
            wanderAround();
        }
        else
        {
            // Stand and stare.
        }
    }

    /**
     * I should look at the work of others. TODO: add also random near building picker
     */
    private static void wanderAround()
    {
        if (!wander.shouldContinueExecuting())
        {
            wander = new EntityAICitizenWander(worker, DEFAULT_SPEED, RANDOM_MODIFIER);
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
            wander = new EntityAICitizenCheckWorkerBuilding(worker, (random.nextBoolean()) ? DEFAULT_SPEED * 1.5D : DEFAULT_SPEED * 2.2D, building, RANDOM_MODIFIER);
            if (wander.shouldExecute())
            {
                wander.startExecuting();
            }
        }
    }

    private static class EntityAICitizenCheckWorkerBuilding extends EntityAICitizenWander
    {
        private static AbstractBuildingWorker building;

        private EntityAICitizenCheckWorkerBuilding(final EntityCitizen citizen, final double speed, final AbstractBuildingWorker building, final double randomModifier)
        {
            super(citizen, speed, randomModifier);
            EntityAICitizenCheckWorkerBuilding.building = building;
        }
        
        @Override
        public void startExecuting()
        {
            super.citizen.getNavigator().tryMoveToBlockPos(building.getLocation(), super.speed);
        }
    }
}