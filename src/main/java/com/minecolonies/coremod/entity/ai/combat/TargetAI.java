package com.minecolonies.coremod.entity.ai.combat;

import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.combat.CombatAIStates;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.combat.threat.ThreatTableEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.List;

import static com.minecolonies.api.util.constant.GuardConstants.DEFAULT_VISION;
import static com.minecolonies.api.util.constant.GuardConstants.Y_VISION;

/**
 * Target search AI
 */
public class TargetAI<T extends Mob & IThreatTableEntity> implements IStateAI
{
    /**
     * The entity this AI runs for
     */
    protected final T user;

    /**
     * Current target reference
     */
    protected LivingEntity target;

    /**
     * Constructor method for AI
     *
     * @param user The creature which is using the AI
     */
    public TargetAI(final T user, final int targetFrequency, final ITickRateStateMachine stateMachine)
    {
        this.user = user;
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.NO_TARGET, this::checkForTarget, () -> CombatAIStates.ATTACKING, 5));
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.NO_TARGET, this::searchNearbyTarget, () -> CombatAIStates.ATTACKING, targetFrequency));
    }

    /**
     * Checks if the current targets is still valid, if not searches a new target. Adds experience if the current target died.
     *
     * @return true if we found a target, false if no target.
     */
    protected boolean checkForTarget()
    {
        if (target != null && !target.isAlive())
        {
            onTargetDied(target);
            target = null;
        }

        final ThreatTableEntry nextTarget = user.getThreatTable().getTarget();
        if (nextTarget == null)
        {
            return false;
        }

        if (isEntityValidTarget(nextTarget.getEntity()))
        {
            if (target != nextTarget.getEntity())
            {
                target = nextTarget.getEntity();
                onTargetChange();
            }

            return true;
        }
        else
        {
            resetTarget();
            return false;
        }
    }

    /**
     * Checks whether the given entity is a valid target to attack.
     *
     * @param target Entity to check
     * @return true if should attack
     */
    public boolean isEntityValidTarget(final LivingEntity target)
    {
        if (target == user || target == null || !target.isAlive() || !isWithinPersecutionDistance(target))
        {
            return false;
        }

        if (target == user.getLastHurtByMob())
        {
            return true;
        }

        return isAttackableTarget(target);
    }

    /**
     * Resets the current target and removes it from all saved targets.
     */
    public void resetTarget()
    {
        if (target == null)
        {
            return;
        }

        if (user.getLastHurtMob() == target)
        {
            user.setLastHurtMob(null);
        }

        if (user.getLastHurtByMob() == target)
        {
            user.setLastHurtByMob(null);
        }

        user.getThreatTable().markInvalidTarget();
        target = null;
    }

    /**
     * Get a target for the guard. First check if we're under attack by anything and switch target if necessary.
     *
     * @return The next IAIState to go to.
     */
    protected boolean searchNearbyTarget()
    {
        if (checkForTarget())
        {
            return true;
        }

        final List<LivingEntity> entities = user.level.getEntitiesOfClass(LivingEntity.class, getSearchArea());

        if (entities.isEmpty())
        {
            return false;
        }

        boolean foundTarget = false;
        for (final LivingEntity entity : entities)
        {
            if (!entity.isAlive())
            {
                continue;
            }

            if (skipSearch(entity))
            {
                return false;
            }

            if (isEntityValidTarget(entity) && user.getSensing().hasLineOfSight(entity))
            {
                user.getThreatTable().addThreat(entity, 0);
                foundTarget = true;
            }
        }

        return foundTarget;
    }

    /**
     * Skips the search if true
     *
     * @param entity checked entity
     * @return true if skip
     */
    protected boolean skipSearch(final LivingEntity entity)
    {
        return false;
    }

    /**
     * Get the {@link AABB} we're searching for targets in.
     *
     * @return the {@link AABB}
     */
    protected AABB getSearchArea()
    {
        final BlockPos raiderPos = user.blockPosition();
        final Direction randomDirection = Direction.from3DDataValue(user.getRandom().nextInt(4) + 2);
        final int searchRange = getSearchRange();
        final double x1 = raiderPos.getX() + (Math.max(searchRange * randomDirection.getStepX() + DEFAULT_VISION, DEFAULT_VISION));
        final double x2 = raiderPos.getX() + (Math.min(searchRange * randomDirection.getStepX() - DEFAULT_VISION, -DEFAULT_VISION));
        final double y1 = raiderPos.getY() + (getYSearchRange());
        final double y2 = raiderPos.getY() - (getYSearchRange());
        final double z1 = raiderPos.getZ() + (Math.max(searchRange * randomDirection.getStepZ() + DEFAULT_VISION, DEFAULT_VISION));
        final double z2 = raiderPos.getZ() + (Math.min(searchRange * randomDirection.getStepZ() - DEFAULT_VISION, -DEFAULT_VISION));

        return new AABB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Get the Y search range
     *
     * @return
     */
    protected int getYSearchRange()
    {
        return Y_VISION;
    }

    /**
     * The search range in blocks, used for determining a shaped aabb entity lookup
     *
     * @return
     */
    protected int getSearchRange()
    {
        return 16;
    }

    /**
     * Whether the target is attackable
     *
     * @param target
     * @return
     */
    protected boolean isAttackableTarget(final LivingEntity target)
    {
        return target instanceof Enemy && !user.getClass().isInstance(target);
    }

    /**
     * Check if the target is within chasing distance
     *
     * @param target
     * @return
     */
    protected boolean isWithinPersecutionDistance(final LivingEntity target)
    {
        return true;
    }

    /**
     * When our previous target has died
     *
     * @param target
     */
    protected void onTargetDied(final LivingEntity target)
    {

    }

    /**
     * Actions on changing to a new target entity
     */
    protected void onTargetChange()
    {

    }
}
