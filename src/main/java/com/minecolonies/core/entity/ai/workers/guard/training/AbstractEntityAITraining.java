package com.minecolonies.core.entity.ai.workers.guard.training;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.jobs.AbstractJob;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * Abstract class for all training AIs.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class AbstractEntityAITraining<J extends AbstractJob<?, J>, B extends AbstractBuilding> extends AbstractEntityAIInteract<J, B>
{
    /**
     * Percentual chance for target search being chosen as target job.
     */
    private static final int TARGET_SEARCH_CHANCE = 30;

    /**
     * 100% chance to compare it with smaller percentages.
     */
    protected static final int ONE_HUNDRED_PERCENT = 100;

    /**
     * The current pathing target to walk to.
     */
    protected BlockPos currentPathingTarget;

    /**
     * State to go to after pathing.
     */
    protected IAIState stateAfterPathing;

    /**
     * How many more ticks we have until next attack.
     */
    protected int currentAttackDelay = 0;

    /**
     * Creates the abstract part of the AI.inte Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAITraining(@NotNull final J job)
    {
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> START_WORKING, 1),
          new AITarget(START_WORKING, () -> DECIDE, 1),
          new AITarget(DECIDE, this::decide, 20),
          new AITarget(TRAINING_WANDER, this::wander, 200),
          new AITarget(GO_TO_TARGET, this::pathToTarget, 20)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide on which state to go to.
     *
     * @return the next state to go to.
     */
    public IAIState decide()
    {
        if (!isSetup())
        {
            return DECIDE;
        }

        if (worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < TARGET_SEARCH_CHANCE)
        {
            return COMBAT_TRAINING;
        }
        return TRAINING_WANDER;
    }

    /**
     * Method to check if the worker is ready to start.
     *
     * @return true if so.
     */
    protected abstract boolean isSetup();

    /**
     * Wander randomly around within the premises of the building.
     *
     * @return the next state to go to.
     */
    private IAIState wander()
    {
        if (!EntityNavigationUtils.walkToRandomPosWithin(worker, 20, 0.6, building.getCorners()))
        {
            return getState();
        }

        return DECIDE;
    }

    /**
     * Walk to the shooting stand position.
     *
     * @return the next state to go to.
     */
    private IAIState pathToTarget()
    {
        if (!walkToWorkPos(currentPathingTarget))
        {
            return getState();
        }
        return stateAfterPathing;
    }

    /**
     * Reduces the attack delay by the given Tickrate
     */
    protected void reduceAttackDelay()
    {
        if (currentAttackDelay > 0)
        {
            currentAttackDelay--;
        }
    }
}
