package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
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
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import java.lang.ref.WeakReference;
import java.util.List;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;

/**
 * Class taking of the abstract guard methods for all fighting AIs.
 *
 * @param <J> the generic job.
 */
public abstract class AbstractEntityAIGuard<J extends AbstractJobGuard<J>, B extends AbstractBuildingGuards>
    extends AbstractEntityAIFight<J, B>
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
     * The timer for sleeping.
     */
    private int sleepTimer = 0;

    /**
     * Timer for the wakeup AI.
     */
    private int wakeTimer = 0;

    /**
     * The sleeping guard we found
     */
    private WeakReference<EntityCitizen> sleepingGuard = new WeakReference<>(null);

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
        super.registerTargets(new AITarget(DECIDE, this::decide, GUARD_TASK_INTERVAL),
            new AITarget(GUARD_PATROL, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
            new AITarget(GUARD_PATROL, this::checkAndAttackTarget, CHECK_TARGET_INTERVAL),
            new AITarget(GUARD_PATROL, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
            new AITarget(GUARD_PATROL, this::decide, GUARD_TASK_INTERVAL),
            new AITarget(GUARD_SLEEP, this::sleep, 1),
            new AITarget(GUARD_SLEEP, this::sleepParticles, PARTICLE_INTERVAL),
            new AITarget(GUARD_WAKE, this::wakeUpGuard, TICKS_SECOND),
            new AITarget(GUARD_FOLLOW, this::decide, GUARD_TASK_INTERVAL),
            new AITarget(GUARD_FOLLOW, this::checkAndAttackTarget, CHECK_TARGET_INTERVAL),
            new AITarget(GUARD_FOLLOW, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
            new AITarget(GUARD_GUARD, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
            new AITarget(GUARD_GUARD, this::decide, GUARD_TASK_INTERVAL),
            new AITarget(GUARD_GUARD, this::checkAndAttackTarget, CHECK_TARGET_INTERVAL),
            new AITarget(GUARD_GUARD, () -> searchNearbyTarget() != null, this::checkAndAttackTarget, SEARCH_TARGET_INTERVAL),
            new AITarget(GUARD_REGEN, this::regen, GUARD_REGEN_INTERVAL),
            new AITarget(HELP_CITIZEN, this::helping, GUARD_TASK_INTERVAL));
        buildingGuards = getOwnBuilding();
    }

    /**
     * Wake up a nearby sleeping guard
     *
     * @return next state
     */
    private IAIState wakeUpGuard()
    {
        if (sleepingGuard.get() == null || !(sleepingGuard.get().getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
            || !sleepingGuard.get().getCitizenJobHandler().getColonyJob(AbstractJobGuard.class).isAsleep())
        {
            return DECIDE;
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
            return DECIDE;
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
        if (worker.getRevengeTarget() != null || target != null)
        {
            return false;
        }

        double chance = 1;
        final MultiplierModifierResearchEffect effect = worker.getCitizenColonyHandler()
            .getColony()
            .getResearchManager()
            .getResearchEffects()
            .getEffect(SLEEP_LESS, MultiplierModifierResearchEffect.class);
        if (effect != null)
        {
            chance = 1 - effect.getEffect();
        }

        // Chance to fall asleep every 10sec, Chance is 1 in (10 + level/2) = 1 in Level1:5,Level2:6 Level6:8 Level 12:11 etc
        if (worker.getRandom().nextInt((int) (worker.getCitizenData().getJobModifier() * 0.5) + 20) == 1
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
            resetTarget();
            worker.setRevengeTarget(null);
            worker.stopRiding();
            worker.setPosition(worker.posX, worker.posY + 1, worker.posZ);
            worker.getCitizenExperienceHandler().addExperience(1);
            return DECIDE;
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
     * Regen at the building and continue when more than half health.
     *
     * @return next state to go to.
     */
    private IAIState regen()
    {
        final AdditionModifierResearchEffect effect = worker.getCitizenColonyHandler()
            .getColony()
            .getResearchManager()
            .getResearchEffects()
            .getEffect(FLEEING_SPEED, AdditionModifierResearchEffect.class);
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
        if (checkForTarget())
        {
            if (hasTool())
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

    /**
     * Guard at a specific position.
     *
     * @return the next state to run into.
     */
    private IAIState guard()
    {
        worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), GUARD_POS_RANGE);
        return GUARD_GUARD;
    }

    /**
     * Follow a player.
     *
     * @return the next state to run into.
     */
    private IAIState follow()
    {
        worker.addPotionEffect(new EffectInstance(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER));
        this.world.getScoreboard()
            .addPlayerToTeam(worker.getName().getFormattedText(),
                new ScorePlayerTeam(this.world.getScoreboard(), TEAM_COLONY_NAME + worker.getCitizenColonyHandler().getColonyId()));

        if (BlockPosUtil.getDistance2D(worker.getPosition(), buildingGuards.getPlayerToFollow()) > MAX_FOLLOW_DERIVATION)
        {
            TeleportHelper.teleportCitizen(worker, worker.getEntityWorld(), buildingGuards.getPlayerToFollow());
            return GUARD_FOLLOW;
        }

        if (buildingGuards.isTightGrouping())
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getPlayerToFollow(), GUARD_FOLLOW_TIGHT_RANGE);
        }
        else
        {
            if (!isWithinPersecutionDistance(buildingGuards.getPlayerToFollow()))
            {
                worker.getNavigator().clearPath();
                worker.getMoveHelper().strafe(0, 0);
            }
            else
            {
                worker.isWorkerAtSiteWithMove(buildingGuards.getPlayerToFollow(), GUARD_FOLLOW_LOSE_RANGE);
            }
        }
        return GUARD_FOLLOW;
    }

    /**
     * Patrol between a list of patrol points.
     *
     * @return the next patrol point to go to.
     */
    public IAIState patrol()
    {
        if (currentPatrolPoint == null)
        {
            currentPatrolPoint = buildingGuards.getNextPatrolTarget(false);
        }

        if (currentPatrolPoint != null
            && (worker.isWorkerAtSiteWithMove(currentPatrolPoint, 3) || worker.getCitizenStuckHandler().isStuck()))
        {
            buildingGuards.arrivedAtPatrolPoint(worker);
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

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return (getOwnBuilding().getTask() == GuardTask.FOLLOW || target != null) ? Integer.MAX_VALUE
            : ACTIONS_UNTIL_DUMPING * getOwnBuilding().getBuildingLevel();
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
        return !isEntityValidTarget(target) && getState() == GUARD_PATROL;
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
            return DECIDE;
        }

        if (target == null || !target.isAlive())
        {
            target = helpCitizen.get().getRevengeTarget();
            if (target == null || !target.isAlive())
            {
                return DECIDE;
            }
        }

        currentPatrolPoint = null;
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
     * Decide what we should do next! Ticked once every 20 Ticks
     *
     * @return the next IAIState.
     */
    protected IAIState decide()
    {
        reduceAttackDelay(GUARD_TASK_INTERVAL * getTickRate());
        switch (buildingGuards.getTask())
        {
            case PATROL:
                return patrol();

            case GUARD:
                return guard();

            case FOLLOW:
                return follow();

            default:
                worker.isWorkerAtSiteWithMove(worker.getCitizenColonyHandler().getWorkBuilding().getPosition(), GUARD_POS_RANGE);
                break;
        }

        return DECIDE;
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
            // Check sight
            if (!worker.canEntityBeSeen(target))
            {
                lastSeen += GUARD_TASK_INTERVAL;
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

            // Move into range
            if (!isInAttackDistance(target.getPosition()))
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

        // Check the revenge target
        if (isEntityValidTargetAndCanbeSeen(worker.getRevengeTarget()))
        {
            target = worker.getRevengeTarget();
            return true;
        }

        return target != null;
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

        if (entity instanceof IMob)
        {
            final MobEntryView entry = buildingGuards.getMobsToAttack().get(entity.getType().getRegistryName());
            if (entry != null && entry.shouldAttack())
            {
                return true;
            }
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
            final UnlockAbilityResearchEffect effect = worker.getCitizenColonyHandler()
                .getColony()
                .getResearchManager()
                .getResearchEffects()
                .getEffect(RETREAT, UnlockAbilityResearchEffect.class);
            if (effect != null)
            {
                resetTarget();
                return GUARD_REGEN;
            }
        }

        if (!checkForTarget())
        {
            return DECIDE;
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
            if (!worker.canEntityBeSeen(entity) || !entity.isAlive())
            {
                continue;
            }

            // Found a sleeping guard nearby
            if (entity instanceof EntityCitizen)
            {
                final EntityCitizen citizen = (EntityCitizen) entity;
                if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard
                    && ((AbstractJobGuard<J>) citizen.getCitizenJobHandler().getColonyJob()).isAsleep())
                {
                    sleepingGuard = new WeakReference<>(citizen);
                    wakeTimer = 0;
                    registerTarget(new AIOneTimeEventTarget(GUARD_WAKE));
                    return null;
                }
            }

            if (isEntityValidTarget(entity))
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
        return BlockPosUtil.getDistanceSquared(getTaskReferencePoint(), entityPos)
            <= Math.pow(getPersecutionDistance() + getAttackRange(), 2);
    }

    /**
     * Calculates the dmg increase per level
     * 
     * @return the level damage.
     */
    public int getLevelDamage()
    {
        if (worker.getCitizenData() == null)
        {
            return 0;
        }
        // Level scaling damage, +1 on 6,12,19,28,38,50,66 ...
        return worker.getCitizenData().getJobModifier() / (5 + worker.getCitizenData().getJobModifier() / 15);
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
            case PATROL:
                return currentPatrolPoint != null ? currentPatrolPoint : worker.getPosition();

            case FOLLOW:
                return buildingGuards.getPlayerToFollow();

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
        final int buildingBonus = building.getBonusVision();

        final Direction randomDirection = Direction.byIndex(worker.getRandom().nextInt(4) + 2);

        final double x1 = worker.getPosition().getX()
            + (Math.max(buildingBonus * randomDirection.getXOffset() + DEFAULT_VISION, DEFAULT_VISION));
        final double x2 = worker.getPosition().getX()
            + (Math.min(buildingBonus * randomDirection.getXOffset() - DEFAULT_VISION, -DEFAULT_VISION));
        final double y1 = worker.getPosition().getY() + (Y_VISION >> 1);
        final double y2 = worker.getPosition().getY() - (Y_VISION << 1);
        final double z1 = worker.getPosition().getZ()
            + (Math.max(buildingBonus * randomDirection.getZOffset() + DEFAULT_VISION, DEFAULT_VISION));
        final double z2 = worker.getPosition().getZ()
            + (Math.min(buildingBonus * randomDirection.getZOffset() - DEFAULT_VISION, -DEFAULT_VISION));

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Method which calculates the possible attack range in Blocks.
     *
     * @return the calculated range.
     */
    protected abstract int getAttackRange();
}
