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
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition","PMD.EmptyIfStmt"})
    public static void doPause(final EntityCitizen w, final AbstractBuildingWorker b)
    {
        worker = w;
        building = b;

        // Jump out if walking.
        if (wander != null && wander.shouldContinueExecuting())
        {
            return;
        }

        // Pick random activity. TODO: add also random near building picker
        final int percent = random.nextInt(100);
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
     * I should look at the work of others.
     */
    private static void wanderAround()
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
    private static void goCheckOwnWorkerBuilding()
    {
        wander = new EntityAICitizenCheckWorkerBuilding(worker, (random.nextBoolean()) ? DEFAULT_SPEED * 1.5D : DEFAULT_SPEED * 2.2D, building, RANDOM_MODIFIER);
        if (wander.shouldExecute())
        {
            wander.startExecuting();
        }
    }
}