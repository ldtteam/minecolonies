package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.workers.util.GuardGear;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobCavalry;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import static com.minecolonies.api.research.util.ResearchConstants.SHIELD_USAGE;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_MAXIMUM;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.GuardConstants.SHIELD_BUILDING_LEVEL_RANGE;
import static com.minecolonies.api.util.constant.GuardConstants.SHIELD_LEVEL_RANGE;
import static com.minecolonies.api.util.constant.TranslationConstants.CAVALRY_NOHORSE;

/**
 * Cavalry AI
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAICavalry extends AbstractEntityAIGuard<JobCavalry, AbstractBuildingGuards>
{

    /**
     * Tick interval for mount lookup and stable lookup AI targets.
     */
    private static final int GUARD_MOUNT_INTERVAL = 50;

    /**
     * Horizontal search radius used when looking for an available cavalry horse.
     */
    private static final int HORSE_SEARCH_RADIUS = 50;

    /**
     * Radius around the stable used to assign each cavalry unit a personal rest center.
     */
    private static final int STABLE_REST_DISPERSION_RADIUS = 10;

    /**
     * Random wander range around each cavalry unit's personal rest center.
     */
    private static final int STABLE_REST_WANDER_RANGE = 5;

    protected CavalryHorseEntity targetMount = null;
    protected BlockPos stablePos = null;
    protected boolean stableChecked = false;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public EntityAICavalry(@NotNull final JobCavalry job)
    {
        super(job);
        super.registerTargets(
            new AITarget(CombatAIStates.FIND_MOUNT, this::findMount, GUARD_MOUNT_INTERVAL),
            new AITarget(CombatAIStates.FIND_STABLE, this::findStable, GUARD_MOUNT_INTERVAL)
        );

        toolsNeeded.add(JobCavalry.getWeaponType());

        for (final List<GuardGear> list : itemsNeeded)
        {
            list.add(new GuardGear(ModEquipmentTypes.shield.get(),
              EquipmentSlot.OFFHAND,
              TOOL_LEVEL_WOOD_OR_GOLD,
              TOOL_LEVEL_MAXIMUM,
              SHIELD_LEVEL_RANGE,
              SHIELD_BUILDING_LEVEL_RANGE));
        }

        new CavalryCombatAI((EntityCitizen) worker, getStateAI(), this);
    }

    /**
     * Decides the AI state the citizen should be in, and transitions as necessary.
     * If the guard isn't mounted, will transition to the mount finding state.
     * Otherwise, will call the super decide method.
     *
     * @return the next AI state.
     */
    protected IAIState decide()
    {
        if (!worker.isPassenger())
        {
            return CombatAIStates.FIND_MOUNT;
        }
    
        return super.decide();
    }

    /**
     * Sleep activity
     *
     * If the guard is mounted, dismounts and clears the horse of the guard's reservation.
     *
     * @return the next state to go into
     */
    protected IAIState sleep()
    {
        if (worker.isPassenger())
        {
            worker.stopRiding();
        }

        return super.sleep();
    }

    /**
     * Find a stable that might have available horses within range.
     *
     * @return the next AI state.
     */
    protected IAIState findStable()
    {
        IColony colony = worker.getCitizenColonyHandler().getColonyOrRegister();

        if (stablePos == null)
        {
            stablePos = colony.getServerBuildingManager().getBestBuilding(worker.blockPosition(), BuildingStable.class);
        }

        if (stablePos != null)
        {
            if (!EntityNavigationUtils.walkToPos(worker, stablePos, 2, true))
            {
                return CombatAIStates.FIND_STABLE;
            } 

            stableChecked = true;

            return CombatAIStates.FIND_MOUNT;
        }

        return CombatAIStates.NO_TARGET;
    }


    /**
     * Finds a horse to ride. If the horse is already assigned, ride it. Otherwise, find the closest horse and assign it to the guard.
     * If no horse is found, return NO_TARGET.
     * If the guard can't reach the horse, return FIND_MOUNT to try again.
     *
     * @return the next state to go to.
     */
    protected IAIState findMount()
    {
        CavalryHorseEntity horse = null;

        if (!validateMountTarget(targetMount))
        {
            // Prefer the mount we had before, if valid.
            UUID mount = job.getMount();
            Level level = building.getColony().getWorld();

            if (mount != null && level instanceof ServerLevel serverLevel)
            {
                final Entity entity = serverLevel.getEntity(mount);
                if (entity instanceof CavalryHorseEntity myHorse && isAvailableFor(myHorse, worker.getUUID()))
                {
                    horse = myHorse;
                }
            }

            // Either we didn't have a horse before, or it is no longer available for some reason. Find a nearby horse.
            if (horse == null)
            {
                horse = findNearestHorse();
            }

            targetMount = horse;
        }

        if (targetMount == null)
        {
            // No cavalry horses found nearby - let's go to the stable.

            if (stableChecked)
            {
                // Already checked the stable - trigger an interaction indicating that the cavalry unit needs a horse.
                worker.getCitizenData().triggerInteraction(new StandardInteraction(
                    Component.translatable(CAVALRY_NOHORSE),
                    ChatPriority.IMPORTANT));

                JobCavalry cav = (JobCavalry) worker.getCitizenData().getJob();
                cav.setMount(null);
                stableChecked = false;

                EntityNavigationUtils.walkToRandomPos(worker, 15, 0.6D);
                setDelay(200);

                return CombatAIStates.FIND_MOUNT;
            }

            return CombatAIStates.FIND_STABLE;
        }
        
        JobCavalry cav = (JobCavalry) worker.getCitizenData().getJob();
        cav.setMount(targetMount.getUUID());

        if (worker.isPassenger())
        {
            // Already riding something
            return CombatAIStates.NO_TARGET;
        }

        targetMount.reserve(worker);

        if (!EntityNavigationUtils.walkToPos(worker, targetMount.blockPosition(), 2, true))
        {
            return CombatAIStates.FIND_MOUNT;
        }

        if (!targetMount.isAlive())
        {
            // Horse is dead... clear it out and try again.
            targetMount.clearFor(worker);
            targetMount = null;
            return CombatAIStates.FIND_MOUNT;
        }

        if (!targetMount.getPassengers().isEmpty() && !targetMount.getPassengers().contains(worker))
        {
            // Horse got a different passenger; releasing reservation
            targetMount.clearFor(worker);
            targetMount = null;
            return CombatAIStates.NO_TARGET;
        }

        boolean mounted = worker.startRiding(targetMount, true);

        if (mounted)
        {
            targetMount.clearFor(worker);
            return CombatAIStates.NO_TARGET;
        }
        else
        {
            return CombatAIStates.FIND_MOUNT;
        }
    }

    /**
     * Patrol between a list of patrol points.
     *
     * @return the next patrol point to go to.
     */
    @Override
    public IAIState patrol()
    {
        if (building instanceof BuildingStable stable && stable.minutesSinceLastPatrol() < building.getSetting(BuildingStable.PATROL_INTERVAL).getValue())
        {
            currentPatrolPoint = null;
            EntityNavigationUtils.walkToRandomPosAround(worker, getStableRestCenter(), STABLE_REST_WANDER_RANGE, 0.6D);

            return null;
        }

        return super.patrol();
    }

    /**
     * Gets a stable-local rest center unique to this cavalry unit.
     *
     * @return the center to wander around while between patrols.
     */
    private BlockPos getStableRestCenter()
    {
        final int hash = worker.getUUID().hashCode();
        final double angle = Math.toRadians(Math.floorMod(hash, 360));
        final int radius = STABLE_REST_DISPERSION_RADIUS / 2 + Math.floorMod(hash / 31, STABLE_REST_DISPERSION_RADIUS / 2 + 1);
        final int x = (int) Math.round(Math.cos(angle) * radius);
        final int z = (int) Math.round(Math.sin(angle) * radius);

        return building.getPosition().offset(x, 0, z);
    }

    /** Validates a horse target for mounting.
     * 
     * The horse is a valid mount if it is not null, passes the horse filter,
     * is on the same level as the worker, and is unreserved or reserved by this worker.
     * 
     * @param horse the horse to validate
     * @return true if the horse is a valid target, false otherwise
     */
    private boolean validateMountTarget(CavalryHorseEntity horse)
    {
        if (horse == null || horse.level() != worker.level()) 
        {
            return false;
        }

        return isAvailableFor(horse, worker.getUUID());
    }

    /** 
     * Available = alive, riderless, adult, tamed, not reserved 
     * 
     * @param h the horse to check
     */
    private static boolean isAvailable(CavalryHorseEntity h)
    {
        return isAvailableFor(h, null);
    }

    /**
     * Checks whether the horse can be mounted by the given reserver.
     *
     * @param h the horse to check
     * @param reserver the reserver allowed to use this horse, or null to require an unreserved horse
     */
    private static boolean isAvailableFor(CavalryHorseEntity h, @Nullable final UUID reserver)
    {
        return h.isAlive() 
            && h.getPassengers().isEmpty() 
            && h instanceof CavalryHorseEntity
            && !h.isBaby() 
            && (h.reservedBy() == null || h.reservedBy().equals(reserver))
            && h.isReadyForCombat()
            && EntitySelector.NO_SPECTATORS.test(h);
    }


    /**
     * Finds the nearest horse to the worker that is available for riding.
     * If a horse is found that is reserved by the worker, it is prioritized over other available horses.
     * @return the nearest available horse, or null if none are found
     */
    @Nullable protected CavalryHorseEntity findNearestHorse()
    {
        final Level level = worker.level();
        if (level.isClientSide) return null;

        final AABB box = worker.getBoundingBox().inflate(HORSE_SEARCH_RADIUS, 20.0, HORSE_SEARCH_RADIUS);
        final UUID me = worker.getUUID();

        // Pull a pool, then sort by reservation priority and distance
        List<CavalryHorseEntity> pool = level.getEntitiesOfClass(CavalryHorseEntity.class, box, EntitySelector.NO_SPECTATORS);

        if (pool.isEmpty())
        {
            return null;
        }

        // 1) Prefer a horse reserved by me, if it is still mountable.
        CavalryHorseEntity mine = pool.stream()
            .filter(h -> h.reservedBy() != null && h.reservedBy().equals(me))
            .filter(h -> isAvailableFor(h, me))
            .min(Comparator.comparingDouble(worker::distanceToSqr))
            .orElse(null);

        if (mine != null) return mine;

        // 2) Otherwise pick the nearest available cavalry horse
        CavalryHorseEntity available =
            pool.stream().filter(EntityAICavalry::isAvailable).min(Comparator.comparingDouble(worker::distanceToSqr)).orElse(null);

        return available;
    }
    
    /**
     * Adds a shield to the list of items that are nice to have if the shield usage research is enabled.
     * @return the list of items nice to have.
     */
    @NotNull
    @Override
    protected List<ItemStorage> itemsNiceToHave()
    {
        final List<ItemStorage> list = super.itemsNiceToHave();
        if (worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SHIELD_USAGE) > 0)
        {
            list.add(new ItemStorage(Items.SHIELD, 1));
        }
        return list;
    }

    /**
     * Check if we can help a citizen
     *
     * @param pos
     * @return true if not fighting/helping already
     */
    public boolean canHelp(final BlockPos pos)
    {
        if (getState() != CombatAIStates.FIND_MOUNT && isWithinPersecutionDistance(pos, getPersecutionDistance()) && canBeInterrupted())
        {
            // Cancel patrolling as soon as someone needs help within range, to ensure the guard doesn't get stuck patrolling somewhere else while under attack.
            currentPatrolPoint = null;

            // Stop sleeping when someone called for help
            stopSleeping();
            return true;
        }
        return super.canHelp(pos);
    }

}
