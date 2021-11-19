package com.minecolonies.coremod.entity.ai.combat;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.combat.CombatAIStates;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.combat.threat.ThreatTableEntry;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.PathResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;

import static com.minecolonies.api.util.constant.Constants.HALF_ROTATION;
import static com.minecolonies.api.util.constant.GuardConstants.TURN_AROUND;

/**
 * Moves the entity and triggers the attack
 */
public class AttackMoveAI<T extends MobEntity & IThreatTableEntity> extends TargetAI<T>
{
    /**
     * Time after which we ignore the target
     */
    private static final long STOP_PERSECUTION_AFTER = 20 * 60;

    /**
     * Current walking path
     */
    private PathResult targetPath = null;

    /**
     * Time at which the next attack happens, worldtime + attackspeed(delay)
     */
    protected long nextAttackTime = 0;

    private int pathAttempts = 0;

    public AttackMoveAI(final T owner, final ITickRateStateMachine stateMachine)
    {
        super(owner, 80, stateMachine);

        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.ATTACKING, () -> true, this::tryAttack, 5));
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.ATTACKING, () -> true, this::move, 10));
    }

    /**
     * Moves towards the target entity and checks visibility and reachability
     *
     * @return true if no more targets
     */
    private IState move()
    {
        if (!checkForTarget())
        {
            return CombatAIStates.NO_TARGET;
        }

        final ThreatTableEntry nextTarget = user.getThreatTable().getTarget();
        if (nextTarget == null)
        {
            return CombatAIStates.NO_TARGET;
        }

        final boolean canSeeTarget = user.getSensing().canSee(target);
        if (canSeeTarget)
        {
            nextTarget.setLastSeen(user.level.getGameTime());
        }
        else if ((user.level.getGameTime() - nextTarget.getLastSeen()) > STOP_PERSECUTION_AFTER)
        {
            resetTarget();
            return null;
        }

        if (!isInAttackDistance(target) || !canSeeTarget)
        {
            user.lookAt(target, (float) HALF_ROTATION, (float) HALF_ROTATION);
            user.getLookControl().setLookAt(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

            if (pathAttempts > 5 || (targetPath != null && targetPath.isDone() && targetPath.failedToReachDestination()))
            {
                pathAttempts = 0;
                targetPath = null;

                user.getThreatTable().addThreat(target, -1);
                if (nextTarget.getThreat() < 5)
                {
                    resetTarget();
                    return null;
                }
            }

            if (targetPath == null ||
                  user.getNavigation().isDone() ||
                  (targetPath.isDone() && targetPath.hasPath() && targetPath.getPath().getTarget().distSqr(target.blockPosition()) > Math.pow(getAttackDistance(), 2) - 1))
            {
                targetPath = moveInAttackPosition(target);
                pathAttempts++;
            }
        }

        return null;
    }

    @Override
    public void resetTarget()
    {
        super.resetTarget();
        targetPath = null;
        pathAttempts = 0;
    }

    /**
     * Whether the target is in attack distance
     *
     * @param target
     * @return
     */
    protected boolean isInAttackDistance(final LivingEntity target)
    {
        return user.distanceTo(target) <= getAttackDistance();
    }

    /**
     * Check and trigger an attack
     *
     * @return
     */
    protected IState tryAttack()
    {
        if (!checkForTarget() || !canAttack())
        {
            return CombatAIStates.NO_TARGET;
        }

        if (nextAttackTime >= user.level.getGameTime() || !isInDistanceForAttack(target))
        {
            return null;
        }

        if (user.getSensing().canSee(target))
        {
            pathAttempts = 0;
            user.lookAt(target, (float) TURN_AROUND, (float) TURN_AROUND);
            doAttack(target);
            nextAttackTime = user.level.getGameTime() + getAttackDelay();
        }

        return null;
    }

    /**
     * Whether we can attack
     *
     * @return
     */
    public boolean canAttack()
    {
        return true;
    }

    /**
     * Check if the target is in distance for beeing attacked
     *
     * @param target target to check
     * @return true if we do attack
     */
    protected boolean isInDistanceForAttack(final LivingEntity target)
    {
        return isInAttackDistance(target);
    }

    /**
     * The actual attack triggered on the target
     *
     * @param target
     */
    protected void doAttack(final LivingEntity target)
    {
        target.hurt(new EntityDamageSource("Default", user), 5);
        user.swing(Hand.MAIN_HAND);
    }

    /**
     * Get the attack distance
     *
     * @return distance in blocks
     */
    protected double getAttackDistance()
    {
        return 5;
    }

    /**
     * Get the delay to next attack in ticks
     *
     * @return default 40 ticks
     */
    protected int getAttackDelay()
    {
        return 40;
    }

    /**
     * Movement to get into attack position
     *
     * @param target target to move towards
     * @return path result
     */
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        return ((AbstractAdvancedPathNavigate) user.getNavigation()).moveToLivingEntity(target, 1d);
    }
}
