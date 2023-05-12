package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.RUNNING;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.SAFE;
import static com.minecolonies.api.util.constant.CitizenConstants.MAX_GUARD_CALL_RANGE;
import static com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenDiseaseHandler.SEEK_DOCTOR_HEALTH;

/**
 * AI task to avoid an Entity class.
 */
public class EntityAICitizenAvoidEntity extends Goal
{
    /**
     * Defines how close the entity has to be to the mob to run away.
     */
    private static final double TOO_CLOSE_TO_MOB = 4D;

    /**
     * The amount of area checks before the citizen assumes it is safe. 40 are done in 10seconds.
     */
    private static final int CHECKS_BEFORE_SAFE = 40;

    /**
     * Move away distances.
     */
    private static final float MIN_MOVE_AWAY_DIST = 5;
    private static final float MED_MOVE_AWAY_DIST = 10;
    private static final float MAX_MOVE_AWAY_DIST = 20;

    /**
     * The entity we are attached to.
     */
    private final EntityCitizen           citizen;
    private final double                  farSpeed;
    private final double                  nearSpeed;
    private final float                   distanceFromEntity;
    private final Class<? extends Entity> targetEntityClass;
    @Nullable
    private       Entity                  closestLivingEntity;

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
    private final ITickRateStateMachine<IAIState> stateMachine;

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
        this.startingPos = entity.blockPosition();
        this.targetEntityClass = targetEntityClass;
        this.distanceFromEntity = distanceFromEntity;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        super.setFlags(EnumSet.of(Flag.MOVE));

        stateMachine = new TickRateStateMachine<>(SAFE, this::onException);

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
            citizen.getNavigation().tryMoveToBlockPos(startingPos, 1);
            return false;
        }

        closestLivingEntity = citizen.getThreatTable().getTargetMob();

        return closestLivingEntity != null && citizen.getSensing().hasLineOfSight(closestLivingEntity) && targetEntityClass.isInstance(closestLivingEntity);
    }

    /**
     * Returns the closest entity to avoid.
     *
     * @return Entity to avoid.
     */
    private Entity getClosestToAvoid()
    {
        if (targetEntityClass == Player.class)
        {
            return CompatibilityUtils.getWorldFromCitizen(citizen).getNearestPlayer(citizen, (double) distanceFromEntity);
        }
        else
        {
            final Optional<Entity> entityOptional = CompatibilityUtils.getWorldFromCitizen(citizen).getEntities(
              citizen,
              citizen.getBoundingBox().inflate(
                (double) distanceFromEntity,
                3.0D,
                (double) distanceFromEntity),
              target -> target.isAlive() && citizen.getSensing().hasLineOfSight(target))
                                                      .stream()
                                                      .filter(targetEntityClass::isInstance)
                                                      .findFirst();

            return entityOptional.orElse(null);
        }
    }

    /**
     * Makes entity move away from {@link #closestLivingEntity}.
     *
     * @return whether the citizen started moving away.
     */
    private boolean performMoveAway()
    {
        if ((moveAwayPath == null || !moveAwayPath.isInProgress()) && citizen.getNavigation().isDone())
        {
            moveAwayPath =
              citizen.getNavigation().moveAwayFromXYZ(citizen.blockPosition().offset(rand.nextInt(2), 0, rand.nextInt(2)), distanceFromEntity + getMoveAwayDist(citizen), nearSpeed, true);
            citizen.getCitizenStatusHandler().setLatestStatus(Component.translatable("com.minecolonies.coremod.status.avoiding"));
            return true;
        }
        return false;
    }

    /**
     * The range to move away.
     *
     * @param citizen the citizen doing the action.
     * @return the distance to run away.
     */
    private float getMoveAwayDist(final AbstractEntityCitizen citizen)
    {
        if (citizen.getHealth() >= citizen.getMaxHealth() - 4)
        {
            return MIN_MOVE_AWAY_DIST;
        }
        else if (citizen.getHealth() >= citizen.getMaxHealth() / 2)
        {
            return MED_MOVE_AWAY_DIST;
        }
        return MAX_MOVE_AWAY_DIST;
    }

    /**
     * Updates the task.
     *
     * @return false if the citizen is fleeing.
     */
    private boolean updateMoving()
    {
        citizen.playMoveAwaySound();

        @Nullable final Entity newClosest = getClosestToAvoid();
        if (newClosest != null)
        {
            if (newClosest.getId() != closestLivingEntity.getId())
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
            if (citizen.distanceTo(closestLivingEntity) < TOO_CLOSE_TO_MOB)
            {
                citizen.getNavigation().setSpeedModifier(nearSpeed);
            }
            else
            {
                citizen.getNavigation().setSpeedModifier(farSpeed);
            }
        }
        return false;
    }

    /**
     * Returns whether the Goal should begin execution of avoiding.
     */
    @Override
    public boolean canUse()
    {
        if (citizen.isCurrentlyFleeing() && citizen.getCitizenJobHandler().shouldRunAvoidance() && (citizen.getCitizenJobHandler().getColonyJob() == null
                                                                                                      || citizen.getCitizenJobHandler().getColonyJob().canAIBeInterrupted()))
        {
            startingPos = citizen.blockPosition();
            fleeingCounter = 0;

            return !(citizen.getHealth() <= SEEK_DOCTOR_HEALTH) || citizen.getCitizenColonyHandler().getColony() == null
                     || citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestBuilding(citizen, BuildingHospital.class) == null;
        }
        return false;
    }

    /**
     * Returns whether an in-progress Goal should continue executing.
     */
    @Override
    public boolean canContinueToUse()
    {
        stateMachine.tick();
        if (citizen.getHealth() <= SEEK_DOCTOR_HEALTH && citizen.getCitizenColonyHandler().getColony() != null
              && citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestBuilding(citizen, BuildingHospital.class) != null)
        {
            return false;
        }

        return citizen.isCurrentlyFleeing() && citizen.getCitizenJobHandler().shouldRunAvoidance();
    }

    /**
     * Resets the task.
     */
    @Override
    public void stop()
    {
        closestLivingEntity = null;
        moveAwayPath = null;
        stateMachine.reset();
    }
}
