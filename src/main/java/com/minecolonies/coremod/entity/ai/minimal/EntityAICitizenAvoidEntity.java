package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.RUNNING;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.SAFE;
import static com.minecolonies.api.util.constant.CitizenConstants.MAX_GUARD_CALL_RANGE;

/**
 * AI task to avoid an Entity class.
 */
public class EntityAICitizenAvoidEntity extends EntityAIBase
{
    /**
     * Defines how close the entity has to be to the mob to run away.
     */
    private static final double                  TOO_CLOSE_TO_MOB = 4D;

    /**
     * The amount of area checks before the citizen assumes it is safe. 40 are done in 10seconds.
     */
    private static final int CHECKS_BEFORE_SAFE = 40;

    /**
     * The entity we are attached to.
     */
    private final        EntityCitizen           citizen;
    private final        double                  farSpeed;
    private final        double                  nearSpeed;
    private final        float                   distanceFromEntity;
    private final        Class<? extends Entity> targetEntityClass;
    @Nullable
    private              Entity                  closestLivingEntity;

    /**
     * Time spent fleeing.
     */
    private int fleeingCounter = 0;

    /**
     * The pathresult of trying to move away
     */
    private PathResult moveAwayPath;

    /**
     * This AI's state changer.
     */
    private final TickRateStateMachine stateMachine;

    /**
     * The blockpos from where the citizen started fleeing.
     */
    private BlockPos startingPos;

    /**
     * Random which is used for running into a random direction.
     */
    private final Random rand = new Random();

    /**
     * Constructor.
     *
     * @param entity             current entity.
     * @param targetEntityClass  entity class we want to avoid.
     * @param distanceFromEntity how far we want to stay away.
     * @param farSpeed           how fast we should move when we are far away.
     * @param nearSpeed          how fast we should move when we are close.
     */
    public EntityAICitizenAvoidEntity(
      @NotNull final EntityCitizen entity, @NotNull final Class<? extends Entity> targetEntityClass,
      final float distanceFromEntity, final double farSpeed, final double nearSpeed)
    {
        super();
        this.citizen = entity;
        this.startingPos = entity.getPosition();
        this.targetEntityClass = targetEntityClass;
        this.distanceFromEntity = distanceFromEntity;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        super.setMutexBits(1);

        stateMachine = new TickRateStateMachine(SAFE, this::onException);

        stateMachine.addTransition(new AITarget(SAFE, this::isEntityClose, () -> RUNNING, 5));
        stateMachine.addTransition(new AITarget(RUNNING, this::updateMoving, () -> SAFE, 5));
    }

    /**
     * Handles any exceptions for this AI.
     *
     * @param e exception to handle
     */
    private void onException(final RuntimeException e)
    {
        Log.getLogger().warn("AvoidAI of:" + citizen.getName() + " threw an Exception:", e);
    }

    /**
     * Check for close entities
     *
     * @return true if we should flee
     */
    public boolean isEntityClose()
    {
        if (!citizen.isCurrentlyFleeing())
        {
            return false;
        }

        fleeingCounter++;

        // reset after 10s no target
        if (fleeingCounter == CHECKS_BEFORE_SAFE)
        {
            fleeingCounter = 0;
            citizen.setFleeingState(false);
            citizen.getNavigator().tryMoveToBlockPos(startingPos, 1);
            return false;
        }

        closestLivingEntity = getClosestToAvoid();

        return closestLivingEntity != null && !(citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard);
    }

    /**
     * Returns the closest entity to avoid.
     *
     * @return Entity to avoid.
     */
    private Entity getClosestToAvoid()
    {
        if (targetEntityClass == EntityPlayer.class)
        {
            return CompatibilityUtils.getWorldFromCitizen(citizen).getClosestPlayerToEntity(citizen, (double) distanceFromEntity);
        }
        else
        {
            final Optional<Entity> entityOptional = CompatibilityUtils.getWorldFromCitizen(citizen).getEntitiesInAABBexcluding(
              citizen,
              citizen.getEntityBoundingBox().grow(
                (double) distanceFromEntity,
                3.0D,
                (double) distanceFromEntity),
              target -> target.isEntityAlive() && citizen.getEntitySenses().canSee(target))
                                                      .stream()
                                                      .filter(targetEntityClass::isInstance)
                                                      .findFirst();

            return entityOptional.orElse(null);
        }
    }

    /**
     * Makes entity move away from {@link #closestLivingEntity}.
     */
    private boolean performMoveAway()
    {
        if ((moveAwayPath == null || !moveAwayPath.isInProgress()) && citizen.getNavigator().noPath())
        {
            moveAwayPath = citizen.getNavigator().moveAwayFromXYZ(citizen.getPosition().add(rand.nextInt(2), 0, rand.nextInt(2)), distanceFromEntity + 5, nearSpeed);
            citizen.getCitizenStatusHandler().setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.avoiding"));
            return true;
        }
        return false;
    }

    /**
     * Updates the task.
     */
    private boolean updateMoving()
    {
        citizen.playMoveAwaySound();

        @Nullable final Entity newClosest = getClosestToAvoid();
        if (newClosest != null)
        {
            if (newClosest.getEntityId() != closestLivingEntity.getEntityId())
            {
                // Calling for help for the new enemy
                citizen.callForHelp(newClosest, MAX_GUARD_CALL_RANGE);
                closestLivingEntity = newClosest;
            }
            performMoveAway();
        }

        if (moveAwayPath == null || !moveAwayPath.isInProgress())
        {
            fleeingCounter = 0;
            return true;
        }
        else
        {
            if (citizen.getDistance(closestLivingEntity) < TOO_CLOSE_TO_MOB)
            {
                citizen.getNavigator().setSpeed(nearSpeed);
            }
            else
            {
                citizen.getNavigator().setSpeed(farSpeed);
            }
        }
        return false;
    }

    /**
     * Returns whether the EntityAIBase should begin execution of avoiding.
     */
    @Override
    public boolean shouldExecute()
    {
        if (citizen.isCurrentlyFleeing())
        {
            startingPos = citizen.getPosition();
            fleeingCounter = 0;
            return true;
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        stateMachine.tick();
        return citizen.isCurrentlyFleeing();
    }

    /**
     * Resets the task.
     */
    @Override
    public void resetTask()
    {
        closestLivingEntity = null;
        moveAwayPath = null;
        stateMachine.reset();
    }
}
