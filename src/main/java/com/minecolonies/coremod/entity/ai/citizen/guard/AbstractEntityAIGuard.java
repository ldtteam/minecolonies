package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.SleepingParticleMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;

/**
 * Class taking of the abstract guard methods for all fighting AIs.
 *
 * @param <J> the generic job.
 */
public abstract class AbstractEntityAIGuard<J extends AbstractJobGuard> extends AbstractEntityAIFight<J>
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
    private static final int MAX_FOLLOW_DERIVATION = 20;

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
    protected EntityLivingBase target = null;

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
     * Interval between guard task updates
     */
    private static final int GUARD_TASK_INTERVAL = 20;

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
        super.registerTargets(
          new AITarget(DECIDE, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_PATROL, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
          new AITarget(GUARD_PATROL, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_SLEEP, this::sleep, 1),
          new AITarget(GUARD_SLEEP, this::sleepParticles, PARTICLE_INTERVAL),
          new AITarget(GUARD_WAKE, this::wakeUpGuard, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_FOLLOW, this::decide, GUARD_TASK_INTERVAL),
          new AITarget(GUARD_GUARD, this::shouldSleep, () -> GUARD_SLEEP, SHOULD_SLEEP_INTERVAL),
          new AITarget(GUARD_GUARD, this::decide, GUARD_TASK_INTERVAL),
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
            worker.getNavigator().moveToEntityLiving(sleepingGuard.get(), getCombatMovementSpeed());
        }
        else
        {
            worker.swingArm(EnumHand.OFF_HAND);
            sleepingGuard.get().attackEntityFrom(new DamageSource("wakeywakey").setDamageBypassesArmor(), 1);
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

        // Chance to fall asleep every 10sec, Chance is 1 in (10 + level/2) = 1 in Level1:5,Level2:6 Level6:8 Level 12:11 etc
        if (worker.getRandom().nextInt((int) (worker.getCitizenExperienceHandler().getLevel() * 0.5) + 10) == 1)
        {
            // Sleep for 2500-3000 ticks
            sleepTimer = worker.getRandom().nextInt(500) + 2500;

            final Entity entity = new SittingEntity(world, worker.posX, worker.posY - 1, worker.posZ, sleepTimer);
            worker.startRiding(entity);
            world.spawnEntity(entity);

            return true;
        }

        return false;
    }

    /**
     * Emits sleeping particles and regens hp when asleep
     */
    private IAIState sleepParticles()
    {
        MineColonies.getNetwork().sendToAllTracking(new SleepingParticleMessage(worker.posX, worker.posY + 2.0d, worker.posZ), worker);

        if (worker.getHealth() < worker.getMaxHealth())
        {
            worker.setHealth(worker.getHealth() + 0.5f);
        }

        return null;
    }

    /**
     * Sleep activity
     */
    private IAIState sleep()
    {
        if (worker.getRevengeTarget() != null || (sleepTimer -= getTickRate()) < 0)
        {
            resetTarget();
            worker.setRevengeTarget(null);
            worker.dismountRidingEntity();
            worker.setPosition(worker.posX, worker.posY + 1, worker.posZ);
            return DECIDE;
        }

        worker.getLookHelper()
          .setLookPosition(worker.posX + worker.getHorizontalFacing().getXOffset(),
            worker.posY + worker.getHorizontalFacing().getYOffset(),
            worker.posZ + worker.getHorizontalFacing().getZOffset(),
            0f,
            -30f);
        return null;
    }

    /**
     * Regen at the building and continue when more than half health.
     *
     * @return next state to go to.
     */
    private IAIState regen()
    {
        if (walkToBuilding())
        {
            return GUARD_REGEN;
        }

        if (worker.getHealth() < ((int) worker.getMaxHealth() * 0.75D) && buildingGuards.shallRetrieveOnLowHealth())
        {
            if (!worker.isPotionActive(MobEffects.REGENERATION))
            {
                worker.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200));
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
     * Guard at a specific position.
     *
     * @return the next state to run into.
     */
    private IAIState guard()
    {
        if (checkForTarget())
        {
            if (hasTool())
            {
                return getAttackState();
            }
            return START_WORKING;
        }

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
        if (checkForTarget())
        {
            if (hasTool())
            {
                return getAttackState();
            }
            return START_WORKING;
        }

        worker.addPotionEffect(new PotionEffect(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER));
        this.world.getScoreboard().addPlayerToTeam(worker.getName(), TEAM_COLONY_NAME + worker.getCitizenColonyHandler().getColonyId());

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
    private IAIState patrol()
    {
        if (checkForTarget())
        {
            if (hasTool())
            {
                return getAttackState();
            }
            return START_WORKING;
        }

        if (currentPatrolPoint == null)
        {
            currentPatrolPoint = buildingGuards.getNextPatrolTarget(null);
        }

        if (currentPatrolPoint != null && (worker.isWorkerAtSiteWithMove(currentPatrolPoint, 2) || worker.getCitizenStuckHandler().isStuck()))
        {
            currentPatrolPoint = buildingGuards.getNextPatrolTarget(currentPatrolPoint);
        }
        return GUARD_PATROL;
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return AbstractBuildingGuards.class;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return (getOwnBuilding(AbstractBuildingGuards.class).getTask() == GuardTask.FOLLOW || target != null)
                 ? Integer.MAX_VALUE
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
            if (!InventoryUtils.hasItemHandlerToolWithLevel(new InvWrapper(getInventory()), toolType, 0, buildingGuards.getMaxToolLevel()))
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
    public void startHelpCitizen(final EntityCitizen citizen, final EntityLivingBase attacker)
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
     */
    private IAIState helping()
    {
        reduceAttackDelay(GUARD_TASK_INTERVAL * getTickRate());
        if (helpCitizen.get() == null || !helpCitizen.get().isCurrentlyFleeing())
        {
            return DECIDE;
        }

        if (target == null || target.isDead)
        {
            target = helpCitizen.get().getRevengeTarget();
            if (target == null || target.isDead)
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
        // Add experience for killed mob
        if (target != null && target.isDead)
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

        // Search a new target
        target = getNearbyTarget();
        return target != null;
    }

    /**
     * Returns whether the entity is a valid target and is visisble.
     *
     * @param entity entity to check
     * @return boolean
     */
    public boolean isEntityValidTargetAndCanbeSeen(final EntityLivingBase entity)
    {
        return isEntityValidTarget(entity) && worker.canEntityBeSeen(entity);
    }

    /**
     * Checks whether the given entity is a valid target to attack.
     *
     * @param entity Entity to check
     * @return true if should attack
     */
    public boolean isEntityValidTarget(final EntityLivingBase entity)
    {
        if (entity == null || entity.isDead || !isWithinPersecutionDistance(entity.getPosition()))
        {
            return false;
        }

        if (entity == worker.getRevengeTarget())
        {
            return true;
        }

        if (entity instanceof IMob)
        {
            final MobEntryView entry = buildingGuards.getMobsToAttack().get(entity.getClass());
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
        if (entity instanceof EntityPlayer && (colony.getPermissions().hasPermission((EntityPlayer) entity, Action.GUARDS_ATTACK)
                                                 || colony.isValidAttackingPlayer((EntityPlayer) entity)))
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
            resetTarget();
            return GUARD_REGEN;
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
    protected EntityLivingBase getNearbyTarget()
    {
        final IColony colony = worker.getCitizenColonyHandler().getColony();
        if (colony == null)
        {
            resetTarget();
            return null;
        }

        final List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, getSearchArea());

        int closest = Integer.MAX_VALUE;
        EntityLivingBase targetEntity = null;

        for (final EntityLivingBase entity : entities)
        {
            if (!worker.canEntityBeSeen(entity) || entity.isDead)
            {
                continue;
            }

            // Found a sleeping guard nearby
            if (entity instanceof EntityCitizen)
            {
                final EntityCitizen citizen = (EntityCitizen) entity;
                if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && ((AbstractJobGuard) citizen.getCitizenJobHandler().getColonyJob()).isAsleep())
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
                final int tempDistance = (int) worker.getPosition().distanceSq(entity.posX, entity.posY, entity.posZ);
                if (tempDistance < closest)
                {
                    closest = tempDistance;
                    targetEntity = entity;
                }
            }
        }

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
        return BlockPosUtil.getMaxDistance2D(worker.getPosition(), position) <= getAttackRange();
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
        return BlockPosUtil.getMaxDistance2D(getTaskReferencePoint(), entityPos) <= getPersecutionDistance() + getAttackRange();
    }

    /**
     * Calculates the dmg increase per level
     */
    public int getLevelDamage()
    {
        if (worker.getCitizenData() == null)
        {
            return 0;
        }
        // Level scaling damage, +1 on 6,12,19,28,38,50,66 ...
        return worker.getCitizenData().getLevel() / (5 + worker.getCitizenData().getLevel() / 15);
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
                return MAX_GUARD_DERIVATION;
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

        final double x1 = worker.getPosition().getX() + (building.getBonusVision() + DEFAULT_VISION);
        final double x2 = worker.getPosition().getX() - (building.getBonusVision() + DEFAULT_VISION);
        final double y1 = worker.getPosition().getY() + (Y_VISION / 2);
        final double y2 = worker.getPosition().getY() - (Y_VISION * 2);
        final double z1 = worker.getPosition().getZ() + (building.getBonusVision() + DEFAULT_VISION);
        final double z2 = worker.getPosition().getZ() - (building.getBonusVision() + DEFAULT_VISION);

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Method which calculates the possible attack range in Blocks.
     *
     * @return the calculated range.
     */
    protected abstract int getAttackRange();
}
