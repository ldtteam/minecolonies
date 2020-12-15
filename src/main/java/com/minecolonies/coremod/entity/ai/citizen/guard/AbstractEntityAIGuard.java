package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.SleepingParticleMessage;
import com.minecolonies.coremod.research.AdditionModifierResearchEffect;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;
import com.minecolonies.coremod.util.NamedDamageSource;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.CitizenConstants.BIG_SATURATION_FACTOR;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;

/**
 * Class taking of the abstract guard methods for all fighting AIs.
 *
 * @param <J> the generic job.
 */
public abstract class AbstractEntityAIGuard<J extends AbstractJobGuard<J>, B extends AbstractBuildingGuards> extends AbstractEntityAIFight<J, B>
{
    /**
     * Entities to kill before dumping into chest.
     */
    private static final int ACTIONS_UNTIL_DUMPING = 5;

    /**
     * Max derivation of current position when patrolling.
     */
    private static final int MAX_PATROL_DERIVATION = 50;

    /**
     * Max derivation of current position when following..
     */
    private static final int MAX_FOLLOW_DERIVATION = 40;

    /**
     * Max derivation of current position when guarding.
     */
    private static final int MAX_GUARD_DERIVATION = 10;

    /**
     * After this amount of ticks not seeing an entity stop persecution.
     */
    private static final int STOP_PERSECUTION_AFTER = TICKS_SECOND * 10;

    /**
     * How far off patrols are alterated to match a raider attack point, sq dist
     */
    private static final double PATROL_DEVIATION_RAID_POINT = 200 * 200;

    /**
     * Max bonus target search range from attack range
     */
    private static final int TARGET_RANGE_ATTACK_RANGE_BONUS = 18;

    /**
     * The amount of time the guard counts as in combat after last combat action
     */
    protected static final int COMBAT_TIME = 30;

    /**
     * How many more ticks we have until next attack.
     */
    protected int currentAttackDelay = 0;

    /**
     * The last time the target was seen.
     */
    private int lastSeen = 0;

    /**
     * The current target for our guard.
     */
    protected LivingEntity target = null;

    /**
     * The current blockPos we're patrolling at.
     */
    private BlockPos currentPatrolPoint = null;

    /**
     * The citizen this guard is helping out.
     */
    private WeakReference<EntityCitizen> helpCitizen = new WeakReference<>(null);

    /**
     * The guard building assigned to this job.
     */
    protected final IGuardBuilding buildingGuards;

    /**
     * The interval between sleeping particles
     */
    private static final int PARTICLE_INTERVAL = 30;

    /**
     * Interval between sleep checks
     */
    private static final int SHOULD_SLEEP_INTERVAL = 200;

    /**
     * Check target interval
     */
    private static final int CHECK_TARGET_INTERVAL = 10;

    /**
     * Search area for target interval
     */
    private static final int SEARCH_TARGET_INTERVAL = 40;

    /**
     * Interval between guard task updates
     */
    private static final int GUARD_TASK_INTERVAL = 100;

    /**
     * Interval between guard regen updates
     */
    private static final int GUARD_REGEN_INTERVAL = 40;

    /**
     * Interval between saturation losses during rallying. BIG_SATURATION_FACTOR loss per interval.
     */
    private static final int RALLY_SATURATION_LOSS_INTERVAL = TICKS_SECOND * 12;

    /**
     * Amount of regular actions before the action counter is increased
     */
    private static final int ACTION_INCREASE_INTERVAL = 10;

    /**
     * The timer for sleeping.
     */
    private int sleepTimer = 0;

    /**
     * Timer for the wakeup AI.
     */
    private int wakeTimer = 0;

    /**
     * Timer for fighting, goes down to 0 when hasnt been fighting for a while
     */
    protected int fighttimer = 0;

    /**
     * The sleeping guard we found
     */
    private WeakReference<EntityCitizen> sleepingGuard = new WeakReference<>(null);

    /**
     * Random generator for this AI.
     */
    private Random randomGenerator = new Random();

    /**
     * Small timer for increasing actions done for continuous actions
     */
    private int regularActionTimer = 0;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          // Note that DECIDE is only here for compatibility purposes. The guards should use GUARD_DECIDE internally.
          new AITarget(DECIDE, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_DECIDE, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_PATROL, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
          new AIEventTarget(AIBlockingEventType.STATE_BLOCKING, this::checkAndAttackTarget, CHECK_TARGET_INTERVAL),
          new AITarget(GUARD_PATROL, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
          new AITarget(GUARD_PATROL, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_SLEEP, this::sleep, 1),
          new AITarget(GUARD_SLEEP, this::sleepParticles, PARTICLE_INTERVAL),
          new AITarget(GUARD_WAKE, this::wakeUpGuard, TICKS_SECOND),
          new AITarget(GUARD_FOLLOW, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_FOLLOW, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
          new AITarget(GUARD_RALLY, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_RALLY, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
          new AITarget(GUARD_RALLY, this::decreaseSaturation, RALLY_SATURATION_LOSS_INTERVAL),
          new AITarget(GUARD_GUARD, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
          new AITarget(GUARD_GUARD, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_GUARD, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
          new AITarget(GUARD_REGEN, this::regen, GUARD_REGEN_INTERVAL),
          new AITarget(HELP_CITIZEN, this::helping, GUARD_TASK_INTERVAL)
        );
        buildingGuards = getOwnBuilding();
    }

    /**
     * Wake up a nearby sleeping guard
     *
     * @return next state
     */
    private IAIState wakeUpGuard()
    {
        if (sleepingGuard.get() == null || !(sleepingGuard.get().getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard) || !sleepingGuard.get()
                                                                                                                                          .getCitizenJobHandler()
                                                                                                                                          .getColonyJob(AbstractJobGuard.class)
                                                                                                                                          .isAsleep())
        {
            return GUARD_DECIDE;
        }

        wakeTimer++;
        // Wait 1 sec
        if (wakeTimer == 1)
        {
            return getState();
        }

        // Move into range
        if (BlockPosUtil.getDistanceSquared(sleepingGuard.get().getPosition(), worker.getPosition()) > 4 && wakeTimer <= 10)
        {
            worker.getNavigator().moveToLivingEntity(sleepingGuard.get(), getCombatMovementSpeed());
        }
        else
        {
            worker.swingArm(Hand.OFF_HAND);
            sleepingGuard.get().attackEntityFrom(new NamedDamageSource("wakeywakey", worker).setDamageBypassesArmor(), 1);
            sleepingGuard.get().setRevengeTarget(worker);
            return GUARD_DECIDE;
        }

        return getState();
    }

    /**
     * Whether the guard should fall asleep.
     *
     * @return true if so
     */
    private boolean shouldSleep()
    {
        if (worker.getRevengeTarget() != null || target != null || fighttimer > 0)
        {
            return false;
        }

        double chance = 1;
        final MultiplierModifierResearchEffect
          effect = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(SLEEP_LESS, MultiplierModifierResearchEffect.class);
        if (effect != null)
        {
            chance = 1 - effect.getEffect();
        }

        // Chance to fall asleep every 10sec, Chance is 1 in (10 + level/2) = 1 in Level1:5,Level2:6 Level6:8 Level 12:11 etc
        if (worker.getRandom().nextInt((int) (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) * 0.5) + 20) == 1
              && worker.getRandom().nextDouble() < chance)
        {
            // Sleep for 2500-3000 ticks
            sleepTimer = worker.getRandom().nextInt(500) + 2500;

            final SittingEntity entity = (SittingEntity) ModEntities.SITTINGENTITY.create(world);
            entity.setPosition(worker.posX, worker.posY - 1f, worker.posZ);
            entity.setMaxLifeTime(sleepTimer);
            world.addEntity(entity);
            worker.startRiding(entity);
            worker.getNavigator().clearPath();

            return true;
        }

        return false;
    }

    /**
     * Emits sleeping particles and regens hp when asleep
     *
     * @return the next state to go into
     */
    private IAIState sleepParticles()
    {
        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(worker.posX, worker.posY + 2.0d, worker.posZ), worker);

        if (worker.getHealth() < worker.getMaxHealth())
        {
            worker.setHealth(worker.getHealth() + 0.5f);
        }

        return null;
    }

    /**
     * Sleep activity
     *
     * @return the next state to go into
     */
    private IAIState sleep()
    {
        if (worker.getRevengeTarget() != null || (sleepTimer -= getTickRate()) < 0)
        {
            stopSleeping();
            return GUARD_DECIDE;
        }

        worker.getLookController()
          .setLookPosition(worker.posX + worker.getHorizontalFacing().getXOffset(),
            worker.posY + worker.getHorizontalFacing().getYOffset(),
            worker.posZ + worker.getHorizontalFacing().getZOffset(),
            0f,
            30f);
        return null;
    }

    /**
     * Stops the guard from sleeping
     */
    private void stopSleeping()
    {
        if (getState() == GUARD_SLEEP)
        {
            resetTarget();
            worker.setRevengeTarget(null);
            worker.stopRiding();
            worker.setPosition(worker.posX, worker.posY + 1, worker.posZ);
            worker.getCitizenExperienceHandler().addExperience(1);
        }
    }

    /**
     * Regen at the building and continue when more than half health.
     *
     * @return next state to go to.
     */
    private IAIState regen()
    {
        final AdditionModifierResearchEffect
          effect = worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(FLEEING_SPEED, AdditionModifierResearchEffect.class);
        if (effect != null)
        {
            if (!worker.isPotionActive(Effects.SPEED))
            {
                worker.addPotionEffect(new EffectInstance(Effects.SPEED, 200, (int) (0 + effect.getEffect())));
            }
        }

        if (walkToBuilding())
        {
            return GUARD_REGEN;
        }

        if (worker.getHealth() < ((int) worker.getMaxHealth() * 0.75D) && buildingGuards.shallRetrieveOnLowHealth())
        {
            if (!worker.isPotionActive(Effects.REGENERATION))
            {
                worker.addPotionEffect(new EffectInstance(Effects.REGENERATION, 200));
            }
            return GUARD_REGEN;
        }

        return START_WORKING;
    }

    /**
     * Get the Attack state to go to.
     *
     * @return the next attack state.
     */
    public abstract IAIState getAttackState();

    /**
     * Checks and attacks the target
     *
     * @return next state
     */
    private IAIState checkAndAttackTarget()
    {
        if (getState() == GUARD_SLEEP || getState() == GUARD_REGEN || getState() == GUARD_ATTACK_PROTECT || getState() == GUARD_ATTACK_PHYSICAL
              || getState() == GUARD_ATTACK_RANGED)
        {
            return null;
        }

        if (checkForTarget())
        {
            if (!hasTool())
            {
                return START_WORKING;
            }

            worker.setCanBeStuck(false);
            worker.getNavigator().getPathingOptions().setCanUseRails(false);
            fighttimer = COMBAT_TIME;
            equipInventoryArmor();
            moveInAttackPosition();
            return getAttackState();
        }

        if (fighttimer > 0)
        {
            fighttimer--;
            if (fighttimer == 0)
            {
                worker.getNavigator().getPathingOptions().setCanUseRails(((EntityCitizen) worker).canPathOnRails());
                worker.setCanBeStuck(true);
            }
        }

        return null;
    }

    /**
     * Guard at a specific position.
     *
     * @return the next state to run into.
     */
    private IAIState guard()
    {
        guardMovement();
        return GUARD_GUARD;
    }

    /**
     * Movement when guarding
     */
    public void guardMovement()
    {
        worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), GUARD_POS_RANGE);
    }

    /**
     * Follow a player.
     *
     * @return the next state to run into.
     */
    private IAIState follow()
    {
        if (BlockPosUtil.getDistance2D(worker.getPosition(), buildingGuards.getPositionToFollow()) > MAX_FOLLOW_DERIVATION)
        {
            TeleportHelper.teleportCitizen(worker, worker.getEntityWorld(), buildingGuards.getPositionToFollow());
            return GUARD_FOLLOW;
        }

        if (buildingGuards.isTightGrouping())
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getPositionToFollow(), GUARD_FOLLOW_TIGHT_RANGE);
        }
        else
        {
            if (!isWithinPersecutionDistance(buildingGuards.getPositionToFollow()))
            {
                worker.getNavigator().clearPath();
                worker.getMoveHelper().strafe(0, 0);
            }
            else
            {
                worker.isWorkerAtSiteWithMove(buildingGuards.getPositionToFollow(), GUARD_FOLLOW_LOSE_RANGE);
            }
        }
        return GUARD_FOLLOW;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMPING * getOwnBuilding().getBuildingLevel();
    }

    /**
     * Rally to a location. This function assumes that the given location is reachable by the worker.
     *
     * @return the next state to run into.
     */
    private IAIState rally(final ILocation location)
    {
        final ICitizenData citizenData = worker.getCitizenData();
        if (!worker.isWorkerAtSiteWithMove(location.getInDimensionLocation()
                                             .add(randomGenerator.nextInt(GUARD_FOLLOW_TIGHT_RANGE) - GUARD_FOLLOW_TIGHT_RANGE / 2,
                                               0,
                                               randomGenerator.nextInt(GUARD_FOLLOW_TIGHT_RANGE) - GUARD_FOLLOW_TIGHT_RANGE / 2),
          GUARD_FOLLOW_TIGHT_RANGE) && citizenData != null)
        {
            if (!worker.isPotionActive(Effects.SPEED))
            {
                // Guards will rally faster with higher skill.
                // Considering 99 is the maximum for any skill, the maximum theoretical getJobModifier() = 99 + 99/4 = 124. We want them to have Speed 5
                // when they're at half-max, so at about skill60. Therefore, divide the skill by 20.
                worker.addPotionEffect(new EffectInstance(Effects.SPEED,
                  5 * TICKS_SECOND,
                  MathHelper.clamp((citizenData.getCitizenSkillHandler().getLevel(Skill.Adaptability) / 20) + 2, 2, 5),
                  false,
                  false));
            }
        }

        return GUARD_RALLY;
    }

    @Override
    protected IAIState startWorkingAtOwnBuilding()
    {
        final ILocation rallyLocation = buildingGuards.getRallyLocation();
        if (rallyLocation != null && rallyLocation.isReachableFromLocation(worker.getLocation()) || !canBeInterrupted())
        {
            return PREPARING;
        }

        // Walks to our building, only when not busy with another task
        return super.startWorkingAtOwnBuilding();
    }

    /**
     * Decrease the saturation while rallying. Rallying is hard work for the guards, so make sure they suffer from it.
     *
     * @return The next state
     */
    protected IAIState decreaseSaturation()
    {
        final ICitizenData citizenData = worker.getCitizenData();

        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenData.getSaturation() * BIG_SATURATION_FACTOR);
        }

        return getState();
    }

    /**
     * Patrol between a list of patrol points.
     *
     * @return the next patrol point to go to.
     */
    public IAIState patrol()
    {
        if (buildingGuards.requiresManualTarget())
        {
            if (currentPatrolPoint == null || worker.isWorkerAtSiteWithMove(currentPatrolPoint, 3))
            {
                if (worker.getRandom().nextInt(5) <= 1)
                {
                    currentPatrolPoint = buildingGuards.getColony().getBuildingManager().getRandomBuilding(b -> true);
                }
                else
                {
                    currentPatrolPoint = findRandomPositionToWalkTo(20);
                }
                
                if (currentPatrolPoint != null)
                {
                    setNextPatrolTarget(currentPatrolPoint);
                }
            }
        }
        else
        {
            if (currentPatrolPoint == null)
            {
                currentPatrolPoint = buildingGuards.getNextPatrolTarget(false);
            }

            if (currentPatrolPoint != null && (worker.isWorkerAtSiteWithMove(currentPatrolPoint, 3)))
            {
                buildingGuards.arrivedAtPatrolPoint(worker);
            }
        }
        return GUARD_PATROL;
    }

    /**
     * Sets the next patrol target, and moves to it if patrolling
     *
     * @param target the next patrol target.
     */
    public void setNextPatrolTarget(final BlockPos target)
    {
        currentPatrolPoint = target;
        if (getState() == GUARD_PATROL)
        {
            worker.isWorkerAtSiteWithMove(currentPatrolPoint, 2);
        }
    }

    /**
     * Check if the worker has the required tool to fight.
     *
     * @return true if so.
     */
    public boolean hasTool()
    {
        for (final ToolType toolType : toolsNeeded)
        {
            if (!InventoryUtils.hasItemHandlerToolWithLevel(getInventory(), toolType, 0, buildingGuards.getMaxToolLevel()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Assigning the guard to help a citizen.
     *
     * @param citizen  the citizen to help.
     * @param attacker the citizens attacker.
     */
    public void startHelpCitizen(final EntityCitizen citizen, final LivingEntity attacker)
    {
        if (canHelp())
        {
            registerTarget(new AIOneTimeEventTarget(HELP_CITIZEN));
            target = attacker;
            helpCitizen = new WeakReference<>(citizen);
        }
    }

    /**
     * Check if we can help a citizen
     *
     * @return true if not fighting/helping already
     */
    public boolean canHelp()
    {
        if (!isEntityValidTarget(target) && (getState() == GUARD_PATROL || getState() == GUARD_SLEEP) && canBeInterrupted())
        {
            // Stop sleeping when someone called for help
            stopSleeping();
            return true;
        }
        return false;
    }

    /**
     * Helping out a citizen, moving into range and setting attack target.
     *
     * @return the next state to go into
     */
    private IAIState helping()
    {
        reduceAttackDelay(GUARD_TASK_INTERVAL * getTickRate());
        if (helpCitizen.get() == null || !helpCitizen.get().isCurrentlyFleeing())
        {
            return GUARD_DECIDE;
        }

        if (target == null || !target.isAlive())
        {
            target = helpCitizen.get().getRevengeTarget();
            if (target == null || !target.isAlive())
            {
                return GUARD_DECIDE;
            }
        }

        currentPatrolPoint = null;
        // Check if we're ready to attack the target
        if (worker.getEntitySenses().canSee(target) && isWithinPersecutionDistance(target.getPosition()))
        {
            target.setRevengeTarget(worker);
            return checkAndAttackTarget();
        }

        // Move towards the target
        moveInAttackPosition();

        return HELP_CITIZEN;
    }

    /**
     * Decide what we should do next! Ticked once every GUARD_TASK_INTERVAL Ticks
     *
     * @return the next IAIState.
     */
    protected IAIState decide()
    {
        reduceAttackDelay(GUARD_TASK_INTERVAL * getTickRate());

        final ILocation rallyLocation = buildingGuards.getRallyLocation();

        if (regularActionTimer++ > ACTION_INCREASE_INTERVAL)
        {
            incrementActionsDone();
            regularActionTimer = 0;
        }

        if (rallyLocation != null || buildingGuards.getTask() == GuardTask.FOLLOW)
        {
            worker.addPotionEffect(new EffectInstance(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER, false, false));
        }
        else
        {
            worker.removeActivePotionEffect(GLOW_EFFECT);
        }

        if (rallyLocation != null && rallyLocation.isReachableFromLocation(worker.getLocation()))
        {
            return rally(rallyLocation);
        }

        switch (buildingGuards.getTask())
        {
            case PATROL:
                return patrol();
            case GUARD:
                return guard();
            case FOLLOW:
                return follow();
            default:
                return PREPARING;
        }
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
            incrementActionsDoneAndDecSaturation();
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
        }

        // Check Current target
        if (isEntityValidTarget(target))
        {
            if (target != worker.getRevengeTarget() && isEntityValidTargetAndCanbeSeen(worker.getRevengeTarget())
                  && worker.getDistanceSq(worker.getRevengeTarget()) < worker.getDistanceSq(target) - 15)
            {
                target = worker.getRevengeTarget();
                onTargetChange();
            }

            // Check sight
            if (!worker.canEntityBeSeen(target))
            {
                lastSeen += 10;
            }
            else
            {
                lastSeen = 0;
            }

            if (lastSeen > STOP_PERSECUTION_AFTER)
            {
                resetTarget();
                return false;
            }

            return true;
        }
        else
        {
            resetTarget();
        }

        // Check the revenge target
        if (isEntityValidTargetAndCanbeSeen(worker.getRevengeTarget()))
        {
            target = worker.getRevengeTarget();
            onTargetChange();
            return true;
        }

        return target != null;
    }

    /**
     * Actions on changing to a new target entity
     */
    protected void onTargetChange()
    {
        for (final ICitizenData citizen : getOwnBuilding().getAssignedCitizen())
        {
            if (citizen.getEntity().isPresent() && citizen.getEntity().get().getRevengeTarget() == null)
            {
                citizen.getEntity().get().setRevengeTarget(target);
            }
        }

        if (target instanceof AbstractEntityMinecoloniesMob)
        {
            for (final Map.Entry<BlockPos, IBuilding> entry : worker.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().entrySet())
            {
                if (entry.getValue() instanceof AbstractBuildingGuards &&
                      worker.getPosition().distanceSq(entry.getKey()) < PATROL_DEVIATION_RAID_POINT)
                {
                    final AbstractBuildingGuards building = (AbstractBuildingGuards) entry.getValue();
                    building.setTempNextPatrolPoint(target.getPosition());
                }
            }
        }
    }

    /**
     * Returns whether the entity is a valid target and is visisble.
     *
     * @param entity entity to check
     * @return boolean
     */
    public boolean isEntityValidTargetAndCanbeSeen(final LivingEntity entity)
    {
        return isEntityValidTarget(entity) && worker.canEntityBeSeen(entity);
    }

    /**
     * Checks whether the given entity is a valid target to attack.
     *
     * @param entity Entity to check
     * @return true if should attack
     */
    public boolean isEntityValidTarget(final LivingEntity entity)
    {
        if (entity == null || !entity.isAlive() || !isWithinPersecutionDistance(entity.getPosition()))
        {
            return false;
        }

        if (entity == worker.getRevengeTarget())
        {
            return true;
        }
        
        final MobEntryView entry = buildingGuards.getMobsToAttack().get(entity.getType().getRegistryName());
        if (entry != null && entry.shouldAttack())
        {
            return true;
        }

        final IColony colony = worker.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            return false;
        }

        // Players
        if (entity instanceof PlayerEntity && (colony.getPermissions().hasPermission((PlayerEntity) entity, Action.GUARDS_ATTACK)
                                                 || colony.isValidAttackingPlayer((PlayerEntity) entity)))
        {
            return true;
        }

        // Other colonies guard citizen attacking the colony
        if (entity instanceof EntityCitizen && colony.isValidAttackingGuard((AbstractEntityCitizen) entity))
        {
            return true;
        }

        return false;
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

        if (worker.getLastAttackedEntity() == target)
        {
            worker.setLastAttackedEntity(null);
        }

        if (worker.getRevengeTarget() == target)
        {
            worker.setRevengeTarget(null);
        }

        target = null;
    }

    /**
     * Move the guard into a good attacking position.
     */
    public abstract void moveInAttackPosition();

    /**
     * Execute pre attack checks to check if worker can attack enemy.
     *
     * @return the next aiState to go to.
     */
    public IAIState preAttackChecks()
    {
        if (!hasMainWeapon())
        {
            resetTarget();
            return START_WORKING;
        }

        if (buildingGuards.shallRetrieveOnLowHealth() && worker.getHealth() < ((int) worker.getMaxHealth() * 0.2D))
        {
            final UnlockAbilityResearchEffect effect =
              worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffect(RETREAT, UnlockAbilityResearchEffect.class);
            if (effect != null)
            {
                resetTarget();
                return GUARD_REGEN;
            }
        }

        if (!checkForTarget())
        {
            return GUARD_DECIDE;
        }

        wearWeapon();

        return getState();
    }

    /**
     * Check if the worker has his main weapon.
     *
     * @return true if so.
     */
    public abstract boolean hasMainWeapon();

    /**
     * Get a target for the guard. First check if we're under attack by anything and switch target if necessary.
     *
     * @return The next IAIState to go to.
     */
    protected LivingEntity searchNearbyTarget()
    {
        final IColony colony = worker.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            resetTarget();
            return null;
        }

        final List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, getSearchArea());

        int closest = Integer.MAX_VALUE;
        LivingEntity targetEntity = null;

        for (final LivingEntity entity : entities)
        {
            if (!entity.isAlive())
            {
                continue;
            }

            // Found a sleeping guard nearby
            if (entity instanceof EntityCitizen)
            {
                final EntityCitizen citizen = (EntityCitizen) entity;
                if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && ((AbstractJobGuard<?>) citizen.getCitizenJobHandler().getColonyJob()).isAsleep()
                      && worker.canEntityBeSeen(entity))
                {
                    sleepingGuard = new WeakReference<>(citizen);
                    wakeTimer = 0;
                    registerTarget(new AIOneTimeEventTarget(GUARD_WAKE));
                    return null;
                }
            }

            if (isEntityValidTarget(entity) && worker.canEntityBeSeen(entity))
            {
                // Find closest
                final int tempDistance = (int) BlockPosUtil.getDistanceSquared(worker.getPosition(), entity.getPosition());
                if (tempDistance < closest)
                {
                    closest = tempDistance;
                    targetEntity = entity;
                }
            }
        }

        target = targetEntity;
        onTargetChange();
        return targetEntity;
    }

    /**
     * Wears the weapon of the guard.
     */
    public abstract void wearWeapon();

    /**
     * Check if a position is within regular attack distance.
     *
     * @param position the position to check.
     * @return true if so.
     */
    public boolean isInAttackDistance(final BlockPos position)
    {
        return BlockPosUtil.getDistanceSquared2D(worker.getPosition(), position) <= getAttackRange() * getAttackRange();
    }

    /**
     * Reduces the attack delay by the given value
     *
     * @param value amount to reduce by
     */
    public void reduceAttackDelay(final int value)
    {
        if (currentAttackDelay > 0)
        {
            currentAttackDelay -= value;
        }
    }

    /**
     * Check if a position is within the allowed persecution distance.
     *
     * @param entityPos the position to check.
     * @return true if so.
     */
    private boolean isWithinPersecutionDistance(final BlockPos entityPos)
    {
        return BlockPosUtil.getDistanceSquared(getTaskReferencePoint(), entityPos) <= Math.pow(getPersecutionDistance() + getAttackRange(), 2);
    }

    /**
     * Get the reference point from which the guard comes.
     *
     * @return the position depending ont he task.
     */
    private BlockPos getTaskReferencePoint()
    {
        final ILocation location = buildingGuards.getRallyLocation();
        if (location != null)
        {
            return buildingGuards.getRallyLocation().getInDimensionLocation();
        }
        switch (buildingGuards.getTask())
        {
            case PATROL:
                return currentPatrolPoint != null ? currentPatrolPoint : worker.getPosition();
            case FOLLOW:
                return buildingGuards.getPositionToFollow();
            default:
                return buildingGuards.getGuardPos();
        }
    }

    /**
     * Returns the block distance at which a guard should chase his target
     *
     * @return the block distance at which a guard should chase his target
     */
    private int getPersecutionDistance()
    {
        if (buildingGuards.getRallyLocation() != null)
        {
            return MAX_FOLLOW_DERIVATION;
        }
        switch (buildingGuards.getTask())
        {
            case PATROL:
                return MAX_PATROL_DERIVATION;
            case FOLLOW:
                return MAX_FOLLOW_DERIVATION;
            default:
                return MAX_GUARD_DERIVATION + (buildingGuards.getGuardType() == ModGuardTypes.knight ? 20 : 0);
        }
    }

    /**
     * Get the {@link AxisAlignedBB} we're searching for targets in.
     *
     * @return the {@link AxisAlignedBB}
     */
    private AxisAlignedBB getSearchArea()
    {
        final IGuardBuilding building = getOwnBuilding();
        final int buildingBonus = building.getBonusVision() + Math.max(TARGET_RANGE_ATTACK_RANGE_BONUS, getAttackRange());

        final Direction randomDirection = Direction.byIndex(worker.getRandom().nextInt(4) + 2);

        final double x1 = worker.getPosition().getX() + (Math.max(buildingBonus * randomDirection.getXOffset() + DEFAULT_VISION, DEFAULT_VISION));
        final double x2 = worker.getPosition().getX() + (Math.min(buildingBonus * randomDirection.getXOffset() - DEFAULT_VISION, -DEFAULT_VISION));
        final double y1 = worker.getPosition().getY() + (Y_VISION >> 1);
        final double y2 = worker.getPosition().getY() - (Y_VISION << 1);
        final double z1 = worker.getPosition().getZ() + (Math.max(buildingBonus * randomDirection.getZOffset() + DEFAULT_VISION, DEFAULT_VISION));
        final double z2 = worker.getPosition().getZ() + (Math.min(buildingBonus * randomDirection.getZOffset() - DEFAULT_VISION, -DEFAULT_VISION));

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Method which calculates the possible attack range in Blocks.
     *
     * @return the calculated range.
     */
    protected abstract int getAttackRange();

    @Override
    public boolean canBeInterrupted()
    {
        if (fighttimer > 0 || getState() == GUARD_RALLY || target != null || buildingGuards.getRallyLocation() != null || buildingGuards.getTask() == GuardTask.FOLLOW)
        {
            return false;
        }
        return super.canBeInterrupted();
    }
}
