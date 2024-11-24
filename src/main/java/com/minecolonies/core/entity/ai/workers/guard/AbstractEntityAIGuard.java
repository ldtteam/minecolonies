package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LookHandler;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.EntityListModule;
import com.minecolonies.core.colony.buildings.modules.MinerLevelManagementModule;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.ai.workers.util.MinerLevel;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.network.messages.client.SleepingParticleMessage;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Random;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.core.colony.buildings.AbstractBuildingGuards.HOSTILE_LIST;

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
    private static final int MAX_PATROL_DERIVATION = 80;

    /**
     * How far off patrols are alterated to match a raider attack point, sq dist
     */
    public static final int PATROL_DEVIATION_RAID_POINT = 40 * 40;

    /**
     * Max derivation of current position when following..
     */
    private static final int MAX_FOLLOW_DERIVATION = 30;

    /**
     * Max derivation of current position when guarding.
     */
    private static final int MAX_GUARD_DERIVATION = 10;

    /**
     * The amount of time the guard counts as in combat after last combat action
     */
    protected static final int COMBAT_TIME = 30 * 20;

    /**
     * The current target for our guard.
     */
    protected LivingEntity target = null;

    /**
     * The current blockPos we're patrolling at.
     */
    private BlockPos currentPatrolPoint = null;

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
     * Interval between guard task updates
     */
    private static final int GUARD_TASK_INTERVAL = 100;

    /**
     * Interval between guard regen updates
     */
    private static final int GUARD_REGEN_INTERVAL = 40;

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
    protected int wakeTimer = 0;

    /**
     * Timer for fighting, goes down to 0 when hasnt been fighting for a while
     */
    protected int fighttimer = 0;

    /**
     * The sleeping guard we found
     */
    protected WeakReference<EntityCitizen> sleepingGuard = new WeakReference<>(null);

    /**
     * Random generator for this AI.
     */
    private Random randomGenerator = new Random();

    /**
     * Small timer for increasing actions done for continuous actions
     */
    private int regularActionTimer = 0;

    /**
     * The last position a guard did some guard task on
     */
    private BlockPos lastGuardActionPos;

    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(DECIDE, CombatAIStates.NO_TARGET, 1),
          new AITarget(CombatAIStates.NO_TARGET, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
          new AITarget(GUARD_SLEEP, this::sleep, 1),
          new AITarget(GUARD_SLEEP, this::sleepParticles, PARTICLE_INTERVAL),
          new AITarget(GUARD_REGEN, this::regen, GUARD_REGEN_INTERVAL),
          new AITarget(GUARD_FLEE, this::flee, 20),
          new AITarget(CombatAIStates.ATTACKING, this::shouldFlee, () -> GUARD_FLEE, GUARD_REGEN_INTERVAL),
            new AITarget(CombatAIStates.NO_TARGET, this::shouldFlee, () -> GUARD_FLEE, GUARD_REGEN_INTERVAL),
          new AITarget(CombatAIStates.NO_TARGET, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_WAKE, this::wakeUpGuard, TICKS_SECOND),

          new AITarget(CombatAIStates.ATTACKING, this::inCombat, 8)
        );

        buildingGuards = building;
        lastGuardActionPos = buildingGuards.getPosition();
    }

    /**
     * Updates fight timer during combat
     */
    private IAIState inCombat()
    {
        if (fighttimer <= 0)
        {
            onCombatEnter();
        }

        if (!hasTool())
        {
            return PREPARING;
        }

        fighttimer = COMBAT_TIME;
        return null;
    }

    /**
     * On combat enter
     */
    private void onCombatEnter()
    {
        worker.setCanBeStuck(false);
        worker.getNavigation().getPathingOptions().setCanUseRails(false);
    }

    /**
     * On combat leave
     */
    private void onCombatLeave()
    {
        worker.getNavigation().getPathingOptions().setCanUseRails(((EntityCitizen) worker).canPathOnRails());
        worker.setCanBeStuck(true);
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
            return CombatAIStates.NO_TARGET;
        }

        wakeTimer++;
        if (wakeTimer > 30)
        {
            return CombatAIStates.NO_TARGET;
        }

        final EntityCitizen sleepingCitizen = sleepingGuard.get();

        // Move into range
        if (BlockPosUtil.getDistanceSquared(sleepingCitizen.blockPosition(), worker.blockPosition()) > 2.25)
        {
            worker.getNavigation().moveToLivingEntity(sleepingCitizen, 1.0);
        }
        else
        {
            worker.swing(InteractionHand.OFF_HAND);
            sleepingCitizen.hurt(world.damageSources().source(DamageSourceKeys.WAKEY, this.worker), 1);
            sleepingCitizen.setLastHurtByMob(worker);
            return CombatAIStates.NO_TARGET;
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
        if (worker.getLastHurtByMob() != null || target != null || fighttimer > 0)
        {
            return false;
        }

        final double chance = 1 / (1 + worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SLEEP_LESS));

        // Chance to fall asleep every 10sec, Chance is 1 in (10 + level/2) = 1 in Level1:5,Level2:6 Level6:8 Level 12:11 etc
        if (worker.getRandom().nextInt((int) (worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) * 0.5) + 20) == 1
              && worker.getRandom().nextDouble() < chance)
        {
            // Sleep for 2500-3000 ticks
            sleepTimer = worker.getRandom().nextInt(500) + 2500;
            worker.getNavigation().stop();
            SittingEntity.sitDown(worker.blockPosition(), worker, sleepTimer);

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
        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(worker.getX(), worker.getY() + 2.0d, worker.getZ()), worker);

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
        if (worker.getLastHurtByMob() != null || (sleepTimer -= getTickRate()) < 0)
        {
            stopSleeping();
            ((EntityCitizen) worker).getThreatTable().removeCurrentTarget();
            worker.setLastHurtByMob(null);
            return CombatAIStates.NO_TARGET;
        }

        worker.getLookControl()
          .setLookAt(worker.getX() + worker.getDirection().getStepX(),
            worker.getY() + worker.getDirection().getStepY(),
            worker.getZ() + worker.getDirection().getStepZ(),
            0f,
            30f);
        ((LookHandler) worker.getLookControl()).setLookAtCooldown(sleepTimer);
        return null;
    }

    /**
     * Stops the guard from sleeping
     */
    private void stopSleeping()
    {
        if (getState() == GUARD_SLEEP)
        {
            worker.stopRiding();
            worker.setPos(worker.getX(), worker.getY() + 1, worker.getZ());
            worker.getCitizenExperienceHandler().addExperience(1);
            ((LookHandler) worker.getLookControl()).setLookAtCooldown(2);
        }
    }

    /**
     * Whether the guard should flee
     *
     * @return
     */
    private boolean shouldFlee()
    {
        if (buildingGuards.shallRetrieveOnLowHealth() && worker.getHealth() < ((int) worker.getMaxHealth() * 0.2D) && worker.distanceToSqr(building.getID().getCenter()) > 20)
        {
            return worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(RETREAT) > 0;
        }

        return false;
    }

    /**
     * Regen at the building and continue when more than half health.
     *
     * @return next state to go to.
     */
    private IAIState regen()
    {
        if (((EntityCitizen) worker).getThreatTable().getTargetMob() != null && ((EntityCitizen) worker).getThreatTable().getTargetMob().distanceTo(worker) < 10)
        {
            return CombatAIStates.ATTACKING;
        }

        if (worker.getHealth() < ((int) worker.getMaxHealth() * 0.75D) && buildingGuards.shallRetrieveOnLowHealth())
        {
            if (!worker.hasEffect(MobEffects.REGENERATION))
            {
                worker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
            }
            return GUARD_REGEN;
        }

        return START_WORKING;
    }

    /**
     * Flee to the building.
     *
     * @return next state to go to.
     */
    private IAIState flee()
    {
        if (!worker.hasEffect(MobEffects.MOVEMENT_SPEED))
        {
            final double effect = worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(FLEEING_SPEED);
            if (effect > 0)
            {
                worker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, (int) (0 + effect)));
            }
        }

        if (walkToBuilding())
        {
            return GUARD_FLEE;
        }

        return GUARD_REGEN;
    }

    /**
     * Guard at a specific position.
     *
     * @return the next state to run into.
     */
    private IAIState guard()
    {
        guardMovement();
        return getState();
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
        if (BlockPosUtil.getDistance2D(worker.blockPosition(), buildingGuards.getPositionToFollow()) > MAX_FOLLOW_DERIVATION)
        {
            TeleportHelper.teleportCitizen(worker, worker.getCommandSenderWorld(), buildingGuards.getPositionToFollow());
            return null;
        }

        if (buildingGuards.isTightGrouping())
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getPositionToFollow(), GUARD_FOLLOW_TIGHT_RANGE);
        }
        else
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getPositionToFollow(), GUARD_FOLLOW_LOSE_RANGE);
        }
        return null;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMPING * building.getBuildingLevel();
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
                                             .offset(randomGenerator.nextInt(GUARD_FOLLOW_TIGHT_RANGE) - GUARD_FOLLOW_TIGHT_RANGE / 2,
                                               0,
                                               randomGenerator.nextInt(GUARD_FOLLOW_TIGHT_RANGE) - GUARD_FOLLOW_TIGHT_RANGE / 2),
          GUARD_FOLLOW_TIGHT_RANGE) && citizenData != null)
        {
            if (!worker.hasEffect(MobEffects.MOVEMENT_SPEED))
            {
                // Guards will rally faster with higher skill.
                // Considering 99 is the maximum for any skill, the maximum theoretical getJobModifier() = 99 + 99/4 = 124. We want them to have Speed 5
                // when they're at half-max, so at about skill60. Therefore, divide the skill by 20.
                worker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                  5 * TICKS_SECOND,
                  Mth.clamp((citizenData.getCitizenSkillHandler().getLevel(Skill.Adaptability) / 20), 2, 5),
                  false,
                  false));
            }
        }

        return null;
    }

    @Override
    protected IAIState startWorkingAtOwnBuilding()
    {
        if (buildingGuards != null)
        {
            buildingGuards.setTempNextPatrolPoint(buildingGuards.getPosition());
        }
        return DECIDE;
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
                    setNextPatrolTargetAndMove(currentPatrolPoint);
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
        return null;
    }

    /**
     * Patrol between all completed nodes in the assigned mine
     *
     * @return the next point to patrol to
     */
    public IAIState patrolMine()
    {
        if (buildingGuards.getMinePos() == null)
        {
            return PREPARING;
        }
        if (currentPatrolPoint == null || worker.isWorkerAtSiteWithMove(currentPatrolPoint, 2))
        {
            final IBuilding building = buildingGuards.getColony().getBuildingManager().getBuilding(buildingGuards.getMinePos());
            if (building != null)
            {
                if (building instanceof BuildingMiner)
                {
                    final BuildingMiner buildingMiner = (BuildingMiner) building;
                    final MinerLevel level = buildingMiner.getFirstModuleOccurance(MinerLevelManagementModule.class).getCurrentLevel();
                    if (level == null)
                    {
                        setNextPatrolTargetAndMove(buildingMiner.getPosition());
                    }
                    else
                    {
                        setNextPatrolTargetAndMove(level.getRandomCompletedNode(buildingMiner));
                    }
                }
                else
                {
                    buildingGuards.getFirstModuleOccurance(ISettingsModule.class).getSetting(AbstractBuildingGuards.GUARD_TASK).set(GuardTaskSetting.PATROL);
                }
            }
            else
            {
                buildingGuards.getFirstModuleOccurance(ISettingsModule.class).getSetting(AbstractBuildingGuards.GUARD_TASK).set(GuardTaskSetting.PATROL);
            }
        }
        return null;
    }

    /**
     * Sets the next patrol target, and moves to it if patrolling
     *
     * @param target the next patrol target.
     */
    public void setNextPatrolTargetAndMove(final BlockPos target)
    {
        currentPatrolPoint = target;
        registerTarget(new AIOneTimeEventTarget(() ->
        {
            if (getState() == CombatAIStates.NO_TARGET)
            {
                return decide();
            }
            return getState();
        }));
    }

    /**
     * Check if the worker has the required tool to fight.
     *
     * @return true if so.
     */
    public boolean hasTool()
    {
        for (final EquipmentTypeEntry toolType : toolsNeeded)
        {
            if (!InventoryUtils.hasItemHandlerEquipmentWithLevel(getInventory(), toolType, 0, buildingGuards.getMaxEquipmentLevel()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Assigning the guard to help a citizen.
     *
     * @param attacker the citizens attacker.
     */
    public void startHelpCitizen(final LivingEntity attacker)
    {
        if (canHelp(attacker.blockPosition()))
        {
            ((IThreatTableEntity) worker).getThreatTable().addThreat(attacker, 20);
            registerTarget(new AIOneTimeEventTarget(CombatAIStates.ATTACKING));
        }
    }

    /**
     * Check if we can help a citizen
     *
     * @param pos
     * @return true if not fighting/helping already
     */
    public boolean canHelp(final BlockPos pos)
    {
        if ((getState() == CombatAIStates.NO_TARGET || getState() == GUARD_SLEEP) && canBeInterrupted())
        {
            if (buildingGuards.getTask().equals(GuardTaskSetting.GUARD) && !isWithinPersecutionDistance(pos, getPersecutionDistance()))
            {
                return false;
            }

            // Stop sleeping when someone called for help
            stopSleeping();
            return true;
        }
        return false;
    }

    /**
     * Decide what we should do next! Ticked once every GUARD_TASK_INTERVAL Ticks
     *
     * @return the next IAIState.
     */
    protected IAIState decide()
    {
        final ILocation rallyLocation = buildingGuards.getRallyLocation();

        if (regularActionTimer++ > ACTION_INCREASE_INTERVAL)
        {
            incrementActionsDone();
            regularActionTimer = 0;
        }

        if (worker.getRandom().nextDouble() < 0.05)
        {
            equipInventoryArmor();
        }

        if (!hasTool())
        {
            return PREPARING;
        }

        if (fighttimer > 0)
        {
            fighttimer -= GUARD_TASK_INTERVAL;
            if (fighttimer <= 0)
            {
                onCombatLeave();
            }
        }
        else
        {
            worker.stopUsingItem();
            lastGuardActionPos = worker.blockPosition();
        }

        if (rallyLocation != null || buildingGuards.getTask().equals(GuardTaskSetting.FOLLOW))
        {
            worker.addEffect(new MobEffectInstance(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER, false, false));
        }
        else
        {
            worker.removeEffect(GLOW_EFFECT);
        }

        if (rallyLocation != null && rallyLocation.isReachableFromLocation(worker.getLocation()))
        {
            return rally(rallyLocation);
        }

        return switch (buildingGuards.getTask())
                 {
                     case GuardTaskSetting.PATROL -> patrol();
                     case GuardTaskSetting.GUARD -> guard();
                     case GuardTaskSetting.FOLLOW -> follow();
                     case GuardTaskSetting.PATROL_MINE -> patrolMine();
                     default -> PREPARING;
                 };
    }

    /**
     * Check if a position is within the allowed persecution distance.
     *
     * @param entityPos the position to check.
     * @return true if so.
     */
    public boolean isWithinPersecutionDistance(final BlockPos entityPos, final double attackRange)
    {
        return BlockPosUtil.getDistanceSquared(getTaskReferencePoint(), entityPos) <= Math.pow(getPersecutionDistance() + attackRange, 2);
    }

    /**
     * Get the reference point from which the guard comes.
     *
     * @return the position depending ont he task.
     */
    private BlockPos getTaskReferencePoint()
    {
        switch (buildingGuards.getTask())
        {
            case GuardTaskSetting.PATROL:
            case GuardTaskSetting.PATROL_MINE:
                return lastGuardActionPos;
            case GuardTaskSetting.FOLLOW:
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
            case GuardTaskSetting.PATROL:
            case GuardTaskSetting.PATROL_MINE:
                return MAX_PATROL_DERIVATION;
            case GuardTaskSetting.FOLLOW:
                return MAX_FOLLOW_DERIVATION;
            default:
                return MAX_GUARD_DERIVATION + (getModuleForJob().getJobEntry() == ModJobs.knight.get() ? 20 : 0);
        }
    }

    @Override
    public boolean canBeInterrupted()
    {
        if (fighttimer > 0 || getState() == CombatAIStates.ATTACKING || worker.getLastAttacker() != null || buildingGuards.getRallyLocation() != null || buildingGuards.getTask()
                                                                                                                                                           .equals(GuardTaskSetting.FOLLOW))
        {
            return false;
        }
        return super.canBeInterrupted();
    }

    /**
     * Set the citizen to wakeup
     *
     * @param citizen
     */
    public void setWakeCitizen(final EntityCitizen citizen)
    {
        sleepingGuard = new WeakReference<>(citizen);
        wakeTimer = 0;
        registerTarget(new AIOneTimeEventTarget(GUARD_WAKE));
    }

    @Override
    public Class<B> getExpectedBuildingClass()
    {
        return (Class<B>) AbstractBuildingGuards.class;
    }

    /**
     * Check whether the target is attackable
     *
     * @param user
     * @param entity
     * @return
     */
    public static boolean isAttackableTarget(final AbstractEntityCitizen user, final LivingEntity entity)
    {
        if (IColonyManager.getInstance().getCompatibilityManager().getAllMonsters().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())) && !user.getCitizenData()
                                                                                                                                                          .getWorkBuilding()
                                                                                                                                                          .getModuleMatching(
                                                                                                                                                            EntityListModule.class,
                                                                                                                                                            m -> m.getId()
                                                                                                                                                                   .equals(
                                                                                                                                                                     HOSTILE_LIST))
                                                                                                                                                          .isEntityInList(
                                                                                                                                                            ForgeRegistries.ENTITY_TYPES.getKey(
                                                                                                                                                              entity.getType())))
        {
            return true;
        }

        final IColony colony = user.getCitizenColonyHandler().getColonyOrRegister();
        if (colony == null)
        {
            return false;
        }

        // Players
        if (entity instanceof Player && (colony.getPermissions().hasPermission((Player) entity, Action.GUARDS_ATTACK)
                                           || colony.isValidAttackingPlayer((Player) entity)))
        {
            return true;
        }

        // Other colonies guard citizen attacking the colony
        if (entity instanceof EntityCitizen otherCitizen && otherCitizen.getCitizenColonyHandler().getColonyId() != colony.getID()
            && colony.isValidAttackingGuard((AbstractEntityCitizen) entity))
        {
            return true;
        }

        return false;
    }
}
