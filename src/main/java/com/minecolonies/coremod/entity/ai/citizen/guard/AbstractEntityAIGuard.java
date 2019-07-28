package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.MobEntryView;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

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
    protected final AbstractBuildingGuards buildingGuards;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public AbstractEntityAIGuard(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(DECIDE, this::decide),
          new AITarget(GUARD_PATROL, this::patrol),
          new AITarget(GUARD_FOLLOW, this::follow),
          new AITarget(GUARD_GUARD, this::guard),
          new AITarget(GUARD_REGEN, this::regen),
          new AITarget(HELP_CITIZEN, this::helping, 20)

        );
        buildingGuards = getOwnBuilding();
    }

    /**
     * Regen at the building and continue when more than half health.
     *
     * @return next state to go to.
     */
    private IAIState regen()
    {
        setDelay(STANDARD_DELAY);
        if (walkToBuilding() || worker.getHealth() < ((int) worker.getMaxHealth() * 0.5D) && buildingGuards.shallRetrieveOnLowHealth())
        {
            return getState();
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
            TeleportHelper.teleportCitizen(worker, worker.world, buildingGuards.getPlayerToFollow());
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
        setDelay(Constants.TICKS_SECOND);
        for (final ToolType toolType : toolsNeeded)
        {
            if (!InventoryUtils.hasItemHandlerToolWithLevel(new InvWrapper(getInventory()), toolType, 0, buildingGuards.getMaxToolLevel()))
            {
                setDelay(STANDARD_DELAY);
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
        return (target == null || target.isDead) && getState() != HELP_CITIZEN;
    }

    /**
     * Helping out a citizen, moving into range and setting attack target.
     */
    private IAIState helping()
    {
        if (helpCitizen.get() == null || !helpCitizen.get().isCurrentlyFleeing())
        {
            return DECIDE;
        }

        if (target == null || target.isDead)
        {
            target = helpCitizen.get().getAttackTarget();
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
        setDelay(STANDARD_DELAY);
        switch (buildingGuards.getTask())
        {
            case PATROL:
                return patrol();
            case GUARD:
                return guard();
            case FOLLOW:
                return follow();
            default:
                worker.isWorkerAtSiteWithMove(worker.getCitizenColonyHandler().getWorkBuilding().getLocation(), GUARD_POS_RANGE);
                break;
        }

        return DECIDE;
    }

    /**
     * Check if the current target is null or death and assign a new one if necessary.
     */
    protected boolean checkForTarget()
    {
        if (target != null && target.isDead)
        {
            incrementActionsDoneAndDecSaturation();
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            target = null;
        }

        target = getTarget();
        if (target != null)
        {
            if (!isInAttackDistance(target.getPosition()))
            {
                if (worker.getNavigator().noPath())
                {
                    moveInAttackPosition();
                }
                //no delay, to make sure he can re-orientate on the way to the target
                setDelay(0);
                return false;
            }
            // No delay(1 tick) before activating Combat logic
            setDelay(0);
            return true;
        }
        return false;
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
            target = null;
            return START_WORKING;
        }

        if (worker.getHealth() < ((int) worker.getMaxHealth() * 0.2D) && buildingGuards.shallRetrieveOnLowHealth())
        {
            target = null;
            setDelay(STANDARD_DELAY);
            return GUARD_REGEN;
        }

        if (worker.getRevengeTarget() != null && !worker.getRevengeTarget().isDead
              && (isInAttackDistance(worker.getRevengeTarget().getPosition()) || isWithinPersecutionDistance(worker.getRevengeTarget().getPosition())))
        {
            target = worker.getRevengeTarget();
        }

        if (target == null || target.isDead || !isWithinPersecutionDistance(target.getPosition()))
        {
            // Clear pathing when target changes
            worker.getNavigator().clearPath();
            worker.getMoveHelper().strafe(0, 0);
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
     * Get a target for the guard.
     * First check if we're under attack by anything and switch target if necessary.
     *
     * @return The next IAIState to go to.
     */
    protected EntityLivingBase getTarget()
    {
        reduceAttackDelay(1);

        if (worker.getLastAttackedEntity() != null && !worker.getLastAttackedEntity().isDead)
        {
            if (!isWithinPersecutionDistance(worker.getLastAttackedEntity().getPosition()))
            {
                worker.setLastAttackedEntity(null);
            }
            target = worker.getLastAttackedEntity();
        }

        if (target != null)
        {
            if ((!worker.canEntityBeSeen(target) && lastSeen < STOP_PERSECUTION_AFTER) || !isWithinPersecutionDistance(target.getPosition()))
            {
                target = null;
            }
            else
            {
                lastSeen++;
                return target;
            }
        }

        final Colony colony = worker.getCitizenColonyHandler().getColony();
        if (colony != null)
        {
            if (!colony.getRaiderManager().getHorde((WorldServer) worker.world).isEmpty() || colony.isColonyUnderAttack())
            {
                for (final CitizenData citizen : colony.getCitizenManager().getCitizens())
                {
                    if (citizen.getCitizenEntity().isPresent())
                    {
                        final EntityLivingBase entity = citizen.getCitizenEntity().get().getLastAttackedEntity();
                        if (entity instanceof AbstractEntityMinecoloniesMob && worker.canEntityBeSeen(entity))
                        {
                            return entity;
                        }
                        else if (entity instanceof EntityCitizen && worker.canEntityBeSeen(entity) && (((EntityCitizen) entity).getCitizenJobHandler()
                                                                                                         .getColonyJob() instanceof AbstractJobGuard))
                        {
                            return entity;
                        }
                        else if (entity instanceof EntityPlayer && worker.canEntityBeSeen(entity))
                        {
                            colony.isValidAttackingPlayer((EntityPlayer) entity);
                        }
                    }
                }
            }

            final List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class, getSearchArea(),
              entity -> buildingGuards.getMobsToAttack()
                          .stream()
                          .filter(MobEntryView::hasAttack)
                          .anyMatch(mobEntry -> mobEntry.getEntityEntry().getEntityClass().isInstance(entity)));


            int closest = Integer.MAX_VALUE;
            EntityLivingBase targetEntity = null;
            for (final EntityLivingBase entity : targets)
            {
                if (worker.canEntityBeSeen(entity) && isWithinPersecutionDistance(entity.getPosition()))
                {
                    if (entity instanceof EntityPlayer && (colony.getPermissions().hasPermission((EntityPlayer) entity, Action.GUARDS_ATTACK)
                                                             || colony.isValidAttackingPlayer((EntityPlayer) entity)))
                    {
                        return entity;
                    }
                    else if (entity instanceof EntityCitizen && colony.isValidAttackingGuard((EntityCitizen) entity))
                    {
                        return entity;
                    }

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

        return null;
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
     * Reduces the attack delay by the given Tickrate
     *
     * @param tickRate rate at which the caller is ticking
     */
    public void reduceAttackDelay(final int tickRate)
    {
        if (currentAttackDelay > 0)
        {
            currentAttackDelay -= tickRate;
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
                return currentPatrolPoint != null ? currentPatrolPoint : worker.getCurrentPosition();
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
        final AbstractBuildingGuards building = getOwnBuilding();

        final double x1 = worker.posX + (building.getBonusVision() + DEFAULT_VISION);
        final double x2 = worker.posX - (building.getBonusVision() + DEFAULT_VISION);
        final double y1 = worker.posY + (Y_VISION / 2);
        final double y2 = worker.posY - (Y_VISION * 2);
        final double z1 = worker.posZ + (building.getBonusVision() + DEFAULT_VISION);
        final double z2 = worker.posZ - (building.getBonusVision() + DEFAULT_VISION);

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Method which calculates the possible attack range in Blocks.
     *
     * @return the calculated range.
     */
    protected abstract int getAttackRange();
}
