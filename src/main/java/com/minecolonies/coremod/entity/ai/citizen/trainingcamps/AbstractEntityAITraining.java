package com.minecolonies.coremod.entity.ai.citizen.trainingcamps;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

/**
 * Abstract class for all training AIs.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class AbstractEntityAITraining<J extends AbstractJob> extends AbstractEntityAIBasic<AbstractJob>
{
    /**
     * Percentual chance for target search being chosen as target job.
     */
    private static final int TARGET_SEARCH_CHANCE = 10;

    /**
     * 100% chance to compare it with smaller percentages.
     */
    protected static final int ONE_HUNDRED_PERCENT = 100;

    /**
     * The building range.
     */
    private AxisAlignedBB range;

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
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAITraining(@NotNull final AbstractJob job)
    {
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, () -> DECIDE),
          new AITarget(DECIDE, this::decide),
          new AITarget(TRAINING_WANDER, this::wander),
          new AITarget(GO_TO_TARGET, this::pathToTarget)
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
        setDelay(STANDARD_DELAY);
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
        setDelay(STANDARD_DELAY);
        if (currentPathingTarget == null)
        {
            currentPathingTarget = getWanderPosition();
        }

        if (!walkToBlock(currentPathingTarget) || worker.getCitizenStuckHandler().isStuck())
        {
            currentPathingTarget = null;
            return DECIDE;
        }

        return TRAINING_WANDER;
    }

    /**
     * Walk to the shooting stand position.
     *
     * @return the next state to go to.
     */
    private IAIState pathToTarget()
    {
        setDelay(STANDARD_DELAY);
        if (walkToBlock(currentPathingTarget, 2))
        {
            return getState();
        }
        return stateAfterPathing;
    }

    /**
     * Get a wander position within the archer training camp to walk to.
     *
     * @return the position or the location of the hut chest if not found.
     */
    private BlockPos getWanderPosition()
    {
        final BlockPos pos = BlockPosUtil.getRandomPosition(world, worker.getPosition(), getOwnBuilding().getLocation());

        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        if (range.intersectsWithXZ(new Vec3d(pos)))
        {
            return pos;
        }

        return getOwnBuilding().getLocation();
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
