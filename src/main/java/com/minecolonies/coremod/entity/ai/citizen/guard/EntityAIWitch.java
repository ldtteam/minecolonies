package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobWitch;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;

//TODO
public class EntityAIWitch extends AbstractEntityAIGuard<JobWitch, AbstractBuildingGuards>
{
    public static final  int    GUARD_ATTACK_INTERVAL                     = 20;
    private static final double STRAFING_SPEED                            = 0.6f;
    private static final int    TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS = 4;
    private static final double SWITCH_STRAFING_DIRECTION                 = 0.3d;
    private static final int    MIN_POTION_DISTANCE                       = 6; //TODO What should this value be?

    /**
     * Whether the guard is moving towards his target
     */
    private boolean movingToTarget = false;

    /**
     * Amount of time the guard has been in one spot.
     */
    private int timeAtSameSpot = 0;

    /**
     * Number of ticks the guard has been way too close to target.
     */
    private int tooCloseNumTicks = 0;

    /**
     * Boolean for fleeing pathfinding
     */
    private boolean fleeing = false;

    /**
     * Indicates if strafing should be clockwise or not.
     */
    private int strafingClockwise = 1;

    /**
     * Amount of time strafing is able to run.
     */
    private int strafingTime = -1;

    /**
     * The path for fleeing
     */
    private PathResult fleePath;

    /**
     * Amount of time the guard has been able to see their target.
     */
    private int timeCanSee = 0;

    /**
     * Last distance to determine if the guard is stuck.
     */
    private double lastDistance = 0.0f;

    /**
     * The current buffing target.
     */
    private LivingEntity buffTarget;
    private int          lastSeenBuff;

    /**
     * The current debuffing target.
     */
    private LivingEntity debuffTarget;
    private int          lastSeenDebuff;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWitch(@NotNull final JobWitch job)
    {
        super(job);
        super.registerTargets(
          new AITarget(WITCH_GUARD_ATTACK_DEBUFF, this::debufEnemies, GUARD_ATTACK_INTERVAL),
          new AITarget(WITCH_GUARD_ATTACK_BUFF, this::buffAllies, GUARD_ATTACK_INTERVAL)
        );
    }

    private IAIState debufEnemies()
    {
        final IAIState state = preAttackChecks();
        if (state != getState())
        {
            worker.getNavigator().clearPath();
            worker.getMoveHelper().strafe(0, 0);
            setDelay(STANDARD_DELAY);
            return state;
        }

        if (worker.getCitizenData() == null)
        {
            return START_WORKING;
        }

        return targetAndThrowAtEntity(buffTarget, WITCH_GUARD_ATTACK_DEBUFF);
    }

    /**
     * Buff allies with speed/strength potions
     */
    protected IAIState buffAllies()
    {
        final IAIState state = preAttackChecks();
        if (state != getState())
        {
            worker.getNavigator().clearPath();
            worker.getMoveHelper().strafe(0, 0);
            setDelay(STANDARD_DELAY);
            return state;
        }

        if (worker.getCitizenData() == null)
        {
            return START_WORKING;
        }

        return targetAndThrowAtEntity(buffTarget, WITCH_GUARD_ATTACK_BUFF);
    }

    private IAIState targetAndThrowAtEntity(final LivingEntity target, final IAIState returnOnSuccess)
    {
        final boolean canSee = worker.getEntitySenses().canSee(target);
        final double sqDistanceToEntity = target.getDistanceSq(worker);

        if (canSee)
        {
            timeCanSee++;
        }
        else
        {
            timeCanSee--;
        }

        if (lastDistance == sqDistanceToEntity)
        {
            timeAtSameSpot++;
        }
        else
        {
            timeAtSameSpot = 0;
        }

        final double sqAttackRange = getAttackRange() * getAttackRange();

        // Stuck
        if (sqDistanceToEntity > sqAttackRange && timeAtSameSpot > 8 || (!canSee && timeAtSameSpot > 8))
        {
            worker.getNavigator().clearPath();
            return DECIDE;
        }
        // Move inside attackrange
        else if (sqDistanceToEntity > sqAttackRange || !canSee)
        {
            if (worker.getNavigator().noPath())
            {
                moveInAttackPosition();
            }
            worker.getMoveHelper().strafe(0, 0);
            movingToTarget = true;
        }
        // Clear chasing when in range
        else if (movingToTarget && sqDistanceToEntity < sqAttackRange)
        {
            worker.getNavigator().clearPath();
            movingToTarget = false;
        }

        // Reset Fleeing status
        if (fleeing && !fleePath.isComputing())
        {
            fleeing = false;
            tooCloseNumTicks = 0;
        }

        // Check if the target is too close
        if (sqDistanceToEntity < RANGED_FLEE_SQDIST)
        {
            tooCloseNumTicks++;
            strafingTime = -1;

            // Fleeing
            if (!fleeing && !movingToTarget && sqDistanceToEntity < RANGED_FLEE_SQDIST && tooCloseNumTicks > 5)
            {
                fleePath = worker.getNavigator().moveAwayFromLivingEntity(target, getAttackRange() / 2.0, getCombatMovementSpeed());
                fleeing = true;
                worker.getMoveHelper().strafe(0, (float) strafingClockwise * 0.2f);
                strafingClockwise *= -1;
            }
        }
        else
        {
            tooCloseNumTicks = 0;
        }

        // Combat movement for guards not on guarding block task
        if (buildingGuards.getTask() != GuardTask.GUARD)
        {
            // Toggle strafing direction randomly if strafing
            if (strafingTime >= TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS)
            {
                if ((double) worker.getRNG().nextFloat() < SWITCH_STRAFING_DIRECTION)
                {
                    strafingClockwise *= -1;
                }
                this.strafingTime = 0;
            }

            // Strafe when we're close enough
            if (sqDistanceToEntity < (sqAttackRange / 2.0) && tooCloseNumTicks < 1)
            {
                strafingTime++;
            }
            else if (sqDistanceToEntity > (sqAttackRange / 2.0) + 5)
            {
                strafingTime = -1;
            }

            // Strafe or flee, when not already fleeing or moving in
            if ((strafingTime > -1) && !fleeing && !movingToTarget)
            {
                worker.getMoveHelper().strafe(0, (float) (getCombatMovementSpeed() * strafingClockwise * STRAFING_SPEED));
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            }
            else
            {
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            }
        }

        if (canSee && sqDistanceToEntity <= sqAttackRange)
        {
            worker.setActiveHand(Hand.MAIN_HAND);
            worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            worker.swingArm(Hand.MAIN_HAND);

            throwPotionAt(target);

            timeCanSee = 0;
            if (returnOnSuccess == GUARD_ATTACK_RANGED)
            {
                target.setRevengeTarget(worker);
            }
            currentAttackDelay = getAttackDelay();
            worker.resetActiveHand();
            worker.decreaseSaturationForContinuousAction();
        }
        else
        {
            /*
             * It is possible the object is higher than guard and guard can't get there.
             * Guard will try to back up to get some distance to be able to shoot target.
             */
            if (target.posY > worker.getPosY() + Y_VISION + Y_VISION)
            {
                fleePath = worker.getNavigator().moveAwayFromLivingEntity(target, 10, getCombatMovementSpeed());
                fleeing = true;
                worker.getMoveHelper().strafe(0, 0);
            }
        }
        lastDistance = sqDistanceToEntity;
        return returnOnSuccess;
    }

    private void throwPotionAt(final LivingEntity target)
    {
        final int level = worker.getCitizenData().getCitizenSkillHandler().getLevel(ModGuardTypes.witch.getSecondarySkill());
        final PotionEntity potionentity = new PotionEntity(worker.world, worker);
        potionentity.setItem(worker.getActiveItemStack());
        final float inaccuracy = 99f / level;
        potionentity.shoot(target.getPosX(), target.getPosY(), target.getPosZ(), 0.5F, inaccuracy);
        worker.world.addEntity(potionentity);
    }

    /**
     * Can be overridden in implementations to return the exact building type the worker expects.
     *
     * @return the building type associated with this AI's worker.
     */
    @Override
    public Class<AbstractBuildingGuards> getExpectedBuildingClass()
    {
        return AbstractBuildingGuards.class;
    }

    /**
     * Get a target for the guard. First check if we're under attack by anything and switch target if necessary.
     *
     * @return The next IAIState to go to.
     */
    @Override
    protected LivingEntity searchNearbyTarget()
    {
        final IColony colony = worker.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            resetTarget();
            return null;
        }

        final List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, getSearchArea());

        boolean isAlly = false;
        int closest = Integer.MAX_VALUE;
        LivingEntity closestTarget = null;

        for (final LivingEntity entity : entities)
        {
            if (!worker.canEntityBeSeen(entity) || !entity.isAlive())
            {
                continue;
            }
            final int tempDistance = (int) BlockPosUtil.getDistanceSquared(worker.getPosition(), entity.getPosition());

            if (isAlly(entity))
            {
                /*if (entity instanceof EntityCitizen)
                {
                    final EntityCitizen citizen = (EntityCitizen) entity;

                    // Found a sleeping guard nearby
                    if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && ((AbstractJobGuard<?>) citizen.getCitizenJobHandler().getColonyJob()).isAsleep())
                    {
                        sleepingGuard = new WeakReference<>(citizen);
                        wakeTimer = 0;
                        registerTarget(new AIOneTimeEventTarget(GUARD_WAKE));
                        return null;
                    }
                }*///TODO should witch wake up other guards?
                if (tempDistance < closest)
                {
                    closest = tempDistance;
                    closestTarget = entity;
                    isAlly = true;
                }
            }
            else if (isEntityValidTarget(entity))
            {
                // Find closest
                if (tempDistance < closest)
                {
                    closest = tempDistance;
                    closestTarget = entity;
                    isAlly = false;
                }
            }
        }

        if (closestTarget != null)
        {
            if (isAlly)
            {
                buffTarget = closestTarget;
                debuffTarget = null;
                return buffTarget;
            }
            else
            {
                debuffTarget = closestTarget;
                buffTarget = null;
                return target;
            }
        }

        return null;
    }

    /**
     * Checks if the current targets is still valid, if not searches a new target. Adds experience if the current target died.
     *
     * @return true if we found a target, false if no target.
     */
    @Override
    protected boolean checkForTarget()
    {
        switch ((AIWorkerState) getState())
        {
            case WITCH_GUARD_ATTACK_BUFF:
                return checkForBuffTarget();
            case WITCH_GUARD_ATTACK_DEBUFF:
                return checkForDebuffTarget();
            default:
                return false;
        }
    }

    /**
     * Checks and attacks the target
     *
     * @return next state
     */
    @Override
    protected IAIState checkAndAttackTarget()
    {
        if (checkForBuffTarget())
        {
            if (hasMainWeapon())
            {
                return WITCH_GUARD_ATTACK_BUFF;
            }
            return START_WORKING;
        }
        else if (checkForDebuffTarget())
        {
            if (hasMainWeapon())
            {
                return WITCH_GUARD_ATTACK_DEBUFF;
            }
            return START_WORKING;
        }
        return null;
    }

    private boolean checkForDebuffTarget()
    {
        if (debuffTarget == null || !debuffTarget.isAlive())
        {
            return false;
        }

        final Collection<EffectInstance> effects = PotionUtils.getFullEffectsFromItem(worker.getHeldItemMainhand());
        if (debuffTarget.getActivePotionEffects().isEmpty() || effects.stream()
                                                                 .map(EffectInstance::getPotion)
                                                                 .noneMatch(effect -> debuffTarget.getActivePotionEffect(effect) != null))
        {
            // Check sight
            if (!worker.canEntityBeSeen(debuffTarget))
            {
                lastSeenDebuff += GUARD_TASK_INTERVAL;
            }
            else
            {
                lastSeenDebuff = 0;
            }

            if (lastSeenDebuff > STOP_PERSECUTION_AFTER)
            {
                resetTarget();
                return false;
            }

            // Move into range
            if (!isInAttackDistance(debuffTarget.getPosition()))
            {
                if (worker.getNavigator().noPath())
                {
                    moveInAttackPosition();
                }
            }

            return true;
        }
        else
        {
            resetTarget();
        }
        return false;
    }

    private boolean checkForBuffTarget()
    {
        if (buffTarget == null || !buffTarget.isAlive())
        {
            return false;
        }

        final Collection<EffectInstance> effects = PotionUtils.getFullEffectsFromItem(worker.getHeldItemMainhand());
        if (buffTarget.getActivePotionEffects().isEmpty() || effects.stream()
                                                               .map(EffectInstance::getPotion)
                                                               .noneMatch(effect -> buffTarget.getActivePotionEffect(effect) != null))
        {
            // Check sight
            if (!worker.canEntityBeSeen(buffTarget))
            {
                lastSeenBuff += GUARD_TASK_INTERVAL;
            }
            else
            {
                lastSeenBuff = 0;
            }

            if (lastSeenBuff > STOP_PERSECUTION_AFTER)
            {
                resetTarget();
                return false;
            }

            // Move into range
            if (!isInAttackDistance(buffTarget.getPosition()))
            {
                if (worker.getNavigator().noPath())
                {
                    moveInAttackPosition();
                }
            }

            return true;
        }
        else
        {
            resetTarget();
        }
        return false;
    }

    @NotNull
    private EffectInstance getBuffEffect()
    {
        return null;//TODO
    }

    @NotNull
    private EffectInstance getDebuffEffect()
    {
        return null;//TODO
    }

    /**
     * Checks if the given {@link LivingEntity} is an ally.
     *
     * @param entity the {@link LivingEntity} to check
     * @return true if the {@link LivingEntity} is an ally
     */
    protected boolean isAlly(final LivingEntity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            return !worker.getCitizenData().getColony().isValidAttackingPlayer((PlayerEntity) entity);
        }
        else if (entity instanceof AbstractEntityCitizen)
        {
            final IColony colony = ((AbstractEntityCitizen) entity).getCitizenData().getColony();
            final IColony ownColony = worker.getCitizenData().getColony();
            if (ownColony.equals(colony))
            {
                return ((AbstractEntityCitizen) entity).getCitizenData().getJob() instanceof AbstractJobGuard;
            }
        }
        return false;
    }

    /**
     * Get the Attack state to go to.
     *
     * @return the next attack state.
     */
    @Override
    public IAIState getAttackState()
    {
        return GUARD_ATTACK_RANGED;
    }

    /**
     * Move the guard into a good attacking position.
     */
    @Override
    public void moveInAttackPosition()
    {
        final LivingEntity target;
        switch ((AIWorkerState) getState())
        {
            case WITCH_GUARD_ATTACK_BUFF:
                target = buffTarget;
                break;
            case WITCH_GUARD_ATTACK_DEBUFF:
            default:
                target = debuffTarget;
        }
        worker.getNavigator().tryMoveToBlockPos(
          worker.getPosition().offset(BlockPosUtil.getXZFacing(target.getPosition(), worker.getPosition()).getOpposite(), 8),
          getCombatMovementSpeed());
    }

    /**
     * Resets the current target and removes it from all saved targets.
     */
    @Override
    public void resetTarget()
    {
        switch ((AIWorkerState) getState())
        {
            case WITCH_GUARD_ATTACK_BUFF:
                buffTarget = null;
                break;
            case WITCH_GUARD_ATTACK_DEBUFF:
                debuffTarget = null;
                break;
            default:
                super.resetTarget();
        }
    }

    /**
     * Check if the worker has his main weapon.
     *
     * @return true if so.
     */
    @Override
    public boolean hasMainWeapon()
    {
        return true;
    }

    /**
     * Wears the weapon of the guard.
     */
    @Override
    public void wearWeapon()
    {
        if (getState() instanceof AIWorkerState)
        {
            final AIWorkerState state = (AIWorkerState) getState();
            final ItemStack stack = new ItemStack(Items.POTION);
            if (state == WITCH_GUARD_ATTACK_BUFF)
            {
                PotionUtils.appendEffects(stack, Collections.singleton(getBuffEffect()));
            }
            else if (state == WITCH_GUARD_ATTACK_DEBUFF)
            {
                PotionUtils.appendEffects(stack, Collections.singleton(getDebuffEffect()));
            }
            else
            {
                return;
            }
            worker.setHeldItem(Hand.MAIN_HAND, stack);
        }
    }

    /**
     * Method which calculates the possible attack range in Blocks.
     *
     * @return the calculated range.
     */
    @Override
    protected int getAttackRange()
    {
        return MIN_POTION_DISTANCE;
    }
}
