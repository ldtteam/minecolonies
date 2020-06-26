package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobWitch;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;

//TODO
public class EntityAIWitch extends AbstractEntityAIGuard<JobWitch, AbstractBuildingGuards>
{
    private static final double             STRAFING_SPEED                            = 0.7f;
    private static final int                TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS = 4;
    private static final double             SWITCH_STRAFING_DIRECTION                 = 0.3d;
    public static final  int                GUARD_ATTACK_INTERVAL                     = 10;
    private static final float              HEALTH_PERCENTAGE_MIN                     = .2f;
    private static final int                MIN_POTION_DISTANCE                       = 5; //TODO ???

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
     * The current healing target.
     */
    private LivingEntity healTarget;
    private int          lastSeenHeal;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWitch(@NotNull final JobWitch job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_ATTACK_RANGED, () -> true, this::attackEnemy, GUARD_ATTACK_INTERVAL),
          new AITarget(GUARD_ATTACK_HEAL, () -> true, this::healAllies, GUARD_ATTACK_INTERVAL),
          new AITarget(GUARD_ATTACK_BUFF, () -> true, this::buffAllies, GUARD_ATTACK_INTERVAL)
        );
    }

    /**
     * Attack enemies with harmfull potions (harming, poison, slowness, ...)
     */
    protected IAIState attackEnemy()
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

        return targetAndThrowAtEntity(target, GUARD_ATTACK_RANGED);
    }

    /**
     * Heal allies with healing/regen potions
     */
    protected IAIState healAllies()
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

        if (helpCitizen.get() == null || !helpCitizen.get().isCurrentlyFleeing())
        {
            final List<LivingEntity> allies = world.getEntitiesWithinAABB(LivingEntity.class,
              new AxisAlignedBB(worker.getPosition()).grow(getPersecutionDistance() + getAttackRange()),
              livingEntity -> !livingEntity.equals(worker) && isAlly(livingEntity));
            LivingEntity target = null;
            for (final LivingEntity ally : allies)
            {
                if (target == null || target.getMaxHealth() / target.getHealth() > ally.getMaxHealth() / ally.getHealth())
                {
                    target = ally;
                }
            }
            if (target == null || target.getMaxHealth() / target.getHealth() > HEALTH_PERCENTAGE_MIN)
            {
                return getAttackState();
            }
            this.target = target;
        }
        else
        {
            return targetAndThrowAtEntity(this.helpCitizen.get(), GUARD_ATTACK_HEAL);
        }
        return targetAndThrowAtEntity(this.target, GUARD_ATTACK_HEAL);
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

        return targetAndThrowAtEntity(buffTarget, GUARD_ATTACK_BUFF);
    }

    @Override
    protected IAIState helping()
    {
        reduceAttackDelay(GUARD_TASK_INTERVAL * getTickRate());
        if (helpCitizen.get() == null || !helpCitizen.get().isCurrentlyFleeing())
        {
            return GUARD_DECIDE;
        }

        if (target == null || !target.isAlive())
        {
            if (helpCitizen.get().getMaxHealth() / helpCitizen.get().getHealth() < HEALTH_PERCENTAGE_MIN)
            {
                return GUARD_ATTACK_HEAL;
            }
            target = helpCitizen.get().getRevengeTarget();
            if (target == null || !target.isAlive())
            {
                return GUARD_DECIDE;
            }
        }

        setNextPatrolTarget(null);
        // Check if we're ready to attack the target
        if (worker.getEntitySenses().canSee(target) && isWithinPersecutionDistance(target.getPosition()))
        {
            target.setRevengeTarget(worker);
            return getAttackState();
        }

        // Move towards the target
        moveInAttackPosition();

        return HELP_CITIZEN;
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

        boolean isBuff = false;
        int closest = Integer.MAX_VALUE;
        LivingEntity closestTarget = null;
        LivingEntity closestHeal = null;

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
                    if (entity.getMaxHealth() / entity.getHealth() < HEALTH_PERCENTAGE_MIN)
                    {
                        closest = tempDistance;
                        closestHeal = entity;
                    }
                    else
                    {
                        closest = tempDistance;
                        closestTarget = entity;
                        isBuff = true;
                    }
                }
            }
            else if (isEntityValidTarget(entity))
            {
                // Find closest
                if (tempDistance < closest)
                {
                    closest = tempDistance;
                    closestTarget = entity;
                    isBuff = false;
                }
            }
        }

        if (closestHeal != null)
        {
            healTarget = closestTarget;
            buffTarget = null;
            target = null;
            return healTarget;
        }

        if (isBuff && buffTarget != null)
        {
            buffTarget = closestTarget;
            healTarget = null;
            target = null;
            return buffTarget;
        }
        else if (closestTarget != null)
        {
            target = closestTarget;
            healTarget = null;
            buffTarget = null;
            return target;
        }

        return null;
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
        worker.setActiveHand(Hand.MAIN_HAND);
        final PotionEntity potionentity = new PotionEntity(worker.world, worker);
        potionentity.setItem(worker.getActiveItemStack());
        final float inaccuracy = 99f / level;
        potionentity.shoot(target.getPosX(), target.getPosY(), target.getPosZ(), 0.5F, inaccuracy);
        worker.world.addEntity(potionentity);
        worker.getActiveItemStack().shrink(1);
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
            case GUARD_ATTACK_BUFF:
                return checkForBuffTarget();
            case GUARD_ATTACK_HEAL:
                return checkForHealTarget();
            case GUARD_ATTACK_RANGED:
            default:
                return super.checkForTarget();
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
        if (checkForHealTarget())
        {
            if (hasMainWeapon())
            {
                return GUARD_ATTACK_HEAL;
            }
            return START_WORKING;
        }
        else if (checkForBuffTarget())
        {
            if (hasMainWeapon())
            {
                return GUARD_ATTACK_BUFF;
            }
            return START_WORKING;
        }
        else if (checkForTarget())
        {
            if (hasMainWeapon())
            {
                for (final ICitizenData citizen : getOwnBuilding().getAssignedCitizen())
                {
                    if (citizen.getCitizenEntity().isPresent() && citizen.getCitizenEntity().get().getRevengeTarget() == null)
                    {
                        citizen.getCitizenEntity().get().setRevengeTarget(target);
                    }
                }
                return getAttackState();
            }
            return START_WORKING;
        }
        return null;
    }

    @Override
    public boolean hasTool()
    {
        if (InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
          stack -> stack.getItem() instanceof ThrowablePotionItem && getValidHealingPotions().contains(PotionUtils.getPotionFromItem(stack))) != -1)
        {
            if (InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
              stack -> stack.getItem() instanceof ThrowablePotionItem && getValidBuffPotions().contains(PotionUtils.getPotionFromItem(stack))) != -1)
            {
                if (InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
                  stack -> stack.getItem() instanceof ThrowablePotionItem && getValidHarmPotions().contains(PotionUtils.getPotionFromItem(stack))) != -1)
                {

                }
            }
        }
        return hasMainWeapon();
    }

    @NotNull
    private Collection<Potion> getValidHarmPotions()
    {
        final int level = getOwnBuilding().getBuildingLevel();
        final Collection<Potion> potions = new HashSet<>();
        switch (level)
        {
            case 5:
                potions.add(Potions.STRONG_POISON);
            case 4:
                potions.add(Potions.LONG_POISON);
            case 3:
                potions.add(Potions.POISON);
            case 2:
                potions.add(Potions.STRONG_HARMING);
            case 1:
                potions.add(Potions.HARMING);
        }
        return potions;
    }

    @NotNull
    private Collection<Potion> getValidBuffPotions()
    {
        final int level = getOwnBuilding().getBuildingLevel();
        final Collection<Potion> potions = new HashSet<>();
        switch (level)
        {
            case 5:
                potions.add(Potions.STRONG_STRENGTH);
                potions.add(Potions.LONG_STRENGTH);
                potions.add(Potions.LONG_FIRE_RESISTANCE);
            case 4:
                potions.add(Potions.FIRE_RESISTANCE);
            case 3:
                potions.add(Potions.STRENGTH);
            case 2:
                potions.add(Potions.STRONG_SWIFTNESS);
                potions.add(Potions.LONG_SWIFTNESS);
            case 1:
                potions.add(Potions.SWIFTNESS);
        }
        return potions;
    }

    @NotNull
    private Collection<Potion> getValidHealingPotions()
    {
        final int level = getOwnBuilding().getBuildingLevel();
        final Collection<Potion> potions = new HashSet<>();
        switch (level)
        {
            case 5:
                potions.add(Potions.STRONG_REGENERATION);
            case 4:
                potions.add(Potions.LONG_REGENERATION);
            case 3:
                potions.add(Potions.REGENERATION);
            case 2:
                potions.add(Potions.STRONG_HEALING);
            case 1:
                potions.add(Potions.HEALING);
        }
        return potions;
    }

    private boolean checkForHealTarget()
    {
        if (healTarget == null || !healTarget.isAlive())
        {
            return false;
        }

        if (!(healTarget.getMaxHealth() / healTarget.getHealth() > HEALTH_PERCENTAGE_MIN))
        {
            // Check sight
            if (!worker.canEntityBeSeen(healTarget))
            {
                lastSeenHeal += GUARD_TASK_INTERVAL;
            }
            else
            {
                lastSeenHeal = 0;
            }

            if (lastSeenHeal > STOP_PERSECUTION_AFTER)
            {
                resetTarget();
                return false;
            }

            // Move into range
            if (!isInAttackDistance(healTarget.getPosition()))
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

        final Potion potion = PotionUtils.getPotionFromItem(worker.getHeldItemMainhand());
        final Collection<EffectInstance> effects = buffTarget.getActivePotionEffects();
        if (effects.isEmpty() || effects.stream()
                                   .map(EffectInstance::getPotion)
                                   .noneMatch(effect -> potion.getEffects().stream().map(EffectInstance::getPotion).anyMatch(effect::equals)))
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
            case GUARD_ATTACK_BUFF:
                target = buffTarget;
                break;
            case GUARD_ATTACK_HEAL:
                target = healTarget;
                break;
            case GUARD_ATTACK_RANGED:
            default:
                target = this.target;
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
            case GUARD_ATTACK_BUFF:
                buffTarget = null;
                break;
            case GUARD_ATTACK_HEAL:
                healTarget = null;
                break;
            case GUARD_ATTACK_RANGED:
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
        return InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
          stack -> stack.getItem() instanceof ThrowablePotionItem && getValidHealingPotions().contains(PotionUtils.getPotionFromItem(stack))) != -1 &&
                 InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
                   stack -> stack.getItem() instanceof ThrowablePotionItem && getValidHarmPotions().contains(PotionUtils.getPotionFromItem(stack))) != -1;
    }

    /**
     * Wears the weapon of the guard.
     */
    @Override
    public void wearWeapon()
    {
        if (getState() instanceof AIWorkerState)
        {
            switch ((AIWorkerState) getState())
            {
                case GUARD_ATTACK_HEAL:
                    equipHealingPotion();
                    break;
                case GUARD_ATTACK_BUFF:
                    equipBuffPotion();
                    break;
                case GUARD_ATTACK_RANGED:
                    if (target != null && target.isEntityUndead())
                    {
                        equipHealingPotion();
                    }
                    else
                    {
                        equipHarmingPotion();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Equip a heling potion.
     */
    protected void equipHealingPotion()
    {
        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
          stack -> stack.getItem() instanceof ThrowablePotionItem && getValidHealingPotions().contains(PotionUtils.getPotionFromItem(stack)));
        if (slot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, slot);
        }
    }

    /**
     * Equip a buff potion.
     */
    protected void equipBuffPotion()
    {
        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
          stack -> stack.getItem() instanceof ThrowablePotionItem && getValidBuffPotions().contains(PotionUtils.getPotionFromItem(stack)));
        if (slot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, slot);
        }
    }

    /**
     * Equip a harming potion.
     */
    protected void equipHarmingPotion()
    {
        final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(),
          stack -> stack.getItem() instanceof ThrowablePotionItem && getValidHarmPotions().contains(PotionUtils.getPotionFromItem(stack)));
        if (slot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, slot);
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
