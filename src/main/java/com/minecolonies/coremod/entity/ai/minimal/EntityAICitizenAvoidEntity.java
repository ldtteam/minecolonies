package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.MAX_GUARD_CALL_RANGE;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAICitizenAvoidEntity.FleeStates.RUNNING;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAICitizenAvoidEntity.FleeStates.SAFE;

/**
 * AI task to avoid an Entity class.
 */
public class EntityAICitizenAvoidEntity implements IStateAI
{
    /**
     * Defines how close the entity has to be to the mob to run away.
     */
    private static final double TOO_CLOSE_TO_MOB = 4D;

    /**
     * The amount of area checks before the citizen assumes it is safe. 40 are done in 10seconds.
     */
    private static final int CHECKS_BEFORE_SAFE = 20 * 10;

    /**
     * Move away distances.
     */
    private static final float MIN_MOVE_AWAY_DIST = 10;
    private static final float MED_MOVE_AWAY_DIST = 20;
    private static final float MAX_MOVE_AWAY_DIST = 30;

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
    private int safeTime = 0;

    /**
     * The pathresult of trying to move away
     */
    private PathResult moveAwayPath;

    public enum FleeStates implements IState
    {
        SAFE,
        RUNNING
    }

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

        citizen.getCitizenAI().addTransition(new AITarget(CitizenAIState.FLEE, () -> true, () -> {
            reset();
            return SAFE;
        }, 1));
        citizen.getCitizenAI().addTransition(new AITarget(SAFE, () -> true, this::isEntityClose, 1));
        citizen.getCitizenAI().addTransition(new AITarget(RUNNING, this::updateMoving, () -> SAFE, 5));
    }

    /**
     * Check for close entities
     *
     * @return true if we should flee
     */
    public IState isEntityClose()
    {
        safeTime++;

        if (safeTime > CHECKS_BEFORE_SAFE)
        {
            return CitizenAIState.IDLE;
        }

        closestLivingEntity = citizen.getThreatTable().getTargetMob();
        if (closestLivingEntity != null && citizen.getSensing().hasLineOfSight(closestLivingEntity) && targetEntityClass.isInstance(closestLivingEntity))
        {
            safeTime = 0;
            startingPos = citizen.blockPosition();
            performMoveAway();
            return RUNNING;
        }

        return SAFE;
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
              citizen.getNavigation()
                .moveAwayFromXYZ(citizen.blockPosition().offset(rand.nextInt(2), 0, rand.nextInt(2)), distanceFromEntity + getMoveAwayDist(citizen), nearSpeed, true);
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
            if (citizen.distanceTo(closestLivingEntity) < TOO_CLOSE_TO_MOB)
            {
                performMoveAway();
            }
            safeTime = 0;
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
     * Resets the task.
     */
    public void reset()
    {
        safeTime = 0;
        if (startingPos != null)
        {
            citizen.getNavigation().tryMoveToBlockPos(startingPos, 1);
        }
        closestLivingEntity = null;
        moveAwayPath = null;
        startingPos = null;
    }
}
