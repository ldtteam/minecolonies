package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianUtils;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Abstract class which contains all the guard basics let it be range, melee or magic.
 */
public abstract class AbstractEntityAIGuard extends AbstractEntityAIInteract<JobGuard>
{
    /**
     * Worker gets this distance times building level away from his building to patrol.
     */
    public static final int PATROL_DISTANCE = 40;
    /**
     * Follow the player if farther than this.
     */
    public static final int FOLLOW_RANGE    = 10;

    /**
     * Distance the guard starts searching.
     */
    protected static final int START_SEARCH_DISTANCE = 5;

    /**
     * The start search distance of the guard to track/attack entities may get more depending on the level.
     */
    private static final double MAX_ATTACK_DISTANCE = 20.0D;

    /**
     * Basic delay after operations.
     */
    private static final int BASE_DELAY = 1;

    /**
     * Max amount the guard can shoot arrows before restocking.
     */
    private static final int BASE_MAX_ATTACKS = 25;

    /**
     * Y range in which the guard detects other entities.
     */
    private static final double HEIGHT_DETECTION_RANGE = 10D;

    /**
     * Max tries to find a position to path to.
     */
    private static final int MAX_TRIES = 20;

    /**
     * Path that close to the patrol target.
     */
    private static final int PATH_CLOSE = 2;

    /**
     * The dump base of actions, will increase depending on level.
     */
    private static final int DUMP_BASE = 5;

    /**
     * Increases the max attacks by this amount per level.
     */
    private static final int ADDITIONAL_MAX_ATTACKS_PER_LEVEL = 5;

    /**
     * After this amount of ticks the citizen should switch the patrolling target because his current one is unreachable.
     */
    private static final int SWITCH_TARGET_AFTER_TICKS = 10;

    /**
     * Low health at which the guards should retrieve.
     */
    private static final double LOW_HEALTH = 2.0;

    /**
     * The length range one patrolling operation can have on x or z.
     */
    private static final int LENGTH_RANGE = 10;

    /**
     * The length range one patrolling operation can have on x or z.
     */
    private static final int IN_TEN = 10;

    /**
     * The length range one patrolling operation can have on y.
     */
    private static final int UP_DOWN_RANGE = 4;

    /**
     * Chance of trying to follow the same direction again.
     */
    private static final int LAST_DIRECTION_CHANCE = 5;

    /**
     * The current target.
     */
    protected EntityLivingBase targetEntity;

    /**
     * Amount of arrows already shot or sword hits dealt.
     */
    protected int attacksExecuted = 0;

    /**
     * The distance the guard is searching entities in currently.
     */
    protected int currentSearchDistance = START_SEARCH_DISTANCE;

    /**
     * Checks if the guard should dump its inventory.
     */
    private int dumpAfterActions = DUMP_BASE;

    /**
     * Current goTo task.
     */
    private BlockPos currentPathTarget;

    /**
     * Containing all close entities.
     */
    private List<Entity> entityList;

    /**
     * Last position the worker has been seen.
     */
    private BlockPos lastPos = null;

    /**
     * Amount of ticks the guard is at the same position.
     */
    private int ticksAtSamePos = 0;

    /**
     * Last direction the guard headed to.
     */
    private Tuple<EnumFacing, EnumFacing> lastDirection = null;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAIGuard(@NotNull final JobGuard job)
    {
        super(job);
        super.registerTargets(
          new AITarget(this::checkIfExecute, this::getState),
          new AITarget(IDLE, () -> START_WORKING),
          new AITarget(START_WORKING, () -> GUARD_RESTOCK),
          new AITarget(GUARD_GATHERING, this::gathering)
        );
    }

    /**
     * Checks if the worker is in a state where he can execute well.
     *
     * @return true if so.
     */
    private boolean checkIfExecute()
    {
        final AbstractBuilding building = getOwnBuilding();
        if (!(building instanceof AbstractBuildingGuards))
        {
            return true;
        }

        if (!((AbstractBuildingGuards) building).shallRetrieveOnLowHealth())
        {
            return false;
        }

        if (worker.getHealth() > LOW_HEALTH)
        {
            return false;
        }

        worker.isWorkerAtSiteWithMove(building.getLocation(), PATH_CLOSE);
        return true;
    }

    /**
     * Goes back to the building and tries to take armour from it when he hasn't in his inventory.
     *
     * @return the next state to go to.
     */
    protected AIState goToBuilding()
    {
        if (walkToBuilding())
        {
            return AIState.GUARD_RESTOCK;
        }

        final AbstractBuildingWorker workBuilding = getOwnBuilding();
        if (workBuilding != null)
        {
            final TileEntityColonyBuilding chest = workBuilding.getTileEntity();

            for (int i = 0; i < workBuilding.getTileEntity().getSizeInventory(); i++)
            {
                final ItemStack stack = chest.getStackInSlot(i);

                if (ItemStackUtils.isEmpty(stack))
                {
                    continue;
                }

                if (stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType) == null)
                {
                    final int emptySlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()),
                      ItemStackUtils::isEmpty);

                    if (emptySlot != -1)
                    {
                        new InvWrapper(worker.getInventoryCitizen()).insertItem(emptySlot, stack, false);
                        chest.setInventorySlotContents(i, ItemStackUtils.EMPTY);
                    }
                }
                dumpAfterActions = DUMP_BASE * workBuilding.getBuildingLevel();
            }
        }
        attacksExecuted = 0;
        return AIState.GUARD_SEARCH_TARGET;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return dumpAfterActions;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the armour have to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        updateArmor();
    }

    /**
     * Updates the equipment. Always take the first item of each type and set it.
     */
    protected void updateArmor()
    {
        worker.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStackUtils.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStackUtils.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStackUtils.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStackUtils.EMPTY);

        for (int i = 0; i < new InvWrapper(worker.getInventoryCitizen()).getSlots(); i++)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(i);

            if (ItemStackUtils.isEmpty(stack))
            {
                new InvWrapper(worker.getInventoryCitizen()).extractItem(i, Integer.MAX_VALUE, false);
                continue;
            }

            if (stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType) == ItemStackUtils.EMPTY)
            {
                worker.setItemStackToSlot(((ItemArmor) stack.getItem()).armorType, stack);
            }
        }
    }

    /**
     * Chooses a target from the list.
     *
     * @return the next state.
     */
    protected AIState getTarget()
    {
        if (entityList.isEmpty())
        {
            return AIState.GUARD_PATROL;
        }

        final Entity entity = entityList.get(0);

        final BlockPos buildingLocation = getOwnBuilding().getLocation();

        //Only attack entities in max patrol distance.
        if (BlockPosUtil.getDistance2D(entity.getPosition(), buildingLocation) < ((AbstractBuildingGuards) getOwnBuilding()).getPatrolDistance())
        {
            if (worker.getEntitySenses().canSee(entity) && (entityList.get(0)).isEntityAlive())
            {
                if (entity instanceof EntityPlayer)
                {
                    if (worker.getColony() != null && worker.getColony().getPermissions().hasPermission((EntityPlayer) entity, Action.GUARDS_ATTACK))
                    {
                        targetEntity = (EntityLivingBase) entity;
                        worker.getNavigator().clearPathEntity();
                        return AIState.GUARD_HUNT_DOWN_TARGET;
                    }
                    entityList.remove(0);
                    setDelay(BASE_DELAY);
                    return AIState.GUARD_GET_TARGET;
                }
                else
                {
                    worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.hunting"));

                    worker.getNavigator().clearPathEntity();
                    targetEntity = (EntityLivingBase) entity;
                    return AIState.GUARD_HUNT_DOWN_TARGET;
                }
            }

            if (!(entityList.get(0)).isEntityAlive())
            {
                return GUARD_GATHERING;
            }
        }

        entityList.remove(0);
        setDelay(BASE_DELAY);
        return AIState.GUARD_GET_TARGET;
    }

    /**
     * Searches for the next target.
     *
     * @return the next AIState.
     */
    protected AIState searchTarget()
    {
        if (huntDownlastAttacker())
        {
            targetEntity = this.worker.getLastAttacker();
            return AIState.GUARD_HUNT_DOWN_TARGET;
        }

        if (targetEntity == null)
        {
            targetEntity = BarbarianUtils.getClosestBarbarianToEntity(this.worker, currentSearchDistance);
        }

        entityList = CompatibilityUtils.getWorld(worker).getEntitiesWithinAABB(EntityMob.class, this.getTargetableArea(currentSearchDistance));
        entityList.addAll(CompatibilityUtils.getWorld(worker).getEntitiesWithinAABB(EntitySlime.class, this.getTargetableArea(currentSearchDistance)));
        entityList.addAll(CompatibilityUtils.getWorld(worker).getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(currentSearchDistance)));

        if (targetEntity != null && targetEntity.isEntityAlive() && worker.getEntitySenses().canSee(targetEntity))
        {
            return AIState.GUARD_HUNT_DOWN_TARGET;
        }

        setDelay(BASE_DELAY);
        if (entityList.isEmpty())
        {
            if (currentSearchDistance < getMaxVision())
            {
                currentSearchDistance += START_SEARCH_DISTANCE;
            }
            else
            {
                currentSearchDistance = START_SEARCH_DISTANCE;
                return AIState.GUARD_PATROL;
            }

            return AIState.GUARD_SEARCH_TARGET;
        }
        return AIState.GUARD_GET_TARGET;
    }

    public boolean huntDownlastAttacker()
    {
        if (this.worker.getLastAttacker() != null && this.worker.getLastAttackerTime() >= worker.ticksExisted - ATTACK_TIME_BUFFER
              && this.worker.getLastAttacker().isEntityAlive())
        {
            return this.worker.getLastAttacker() != null && this.worker.canEntityBeSeen(this.worker.getLastAttacker());
        }
        worker.setLastAttacker(null);
        return false;
    }

    private AxisAlignedBB getTargetableArea(final double range)
    {
        return this.worker.getEntityBoundingBox().expand(range, HEIGHT_DETECTION_RANGE, range);
    }

    /**
     * Getter for the vision or attack distance.
     *
     * @return the max vision.
     */
    private double getMaxVision()
    {
        final AbstractBuildingGuards guardTower = (AbstractBuildingGuards) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ATTACK_DISTANCE + guardTower.getBonusVision());
    }

    /**
     * Getter calculating how many arrows the guard may shoot or deal sword hits until restock.
     *
     * @return the amount.
     */
    protected int getMaxAttacksUntilRestock()
    {
        final AbstractBuildingGuards guardTower = (AbstractBuildingGuards) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (BASE_MAX_ATTACKS + guardTower.getBuildingLevel() * ADDITIONAL_MAX_ATTACKS_PER_LEVEL);
    }

    /**
     * Lets the guard patrol inside the colony area searching for mobs.
     *
     * @return the next state to go.
     */
    protected AIState patrol()
    {
        worker.setAIMoveSpeed(1);
        final AbstractBuilding building = getOwnBuilding();

        if (worker.getPosition().equals(lastPos))
        {
            ticksAtSamePos++;
        }
        else
        {
            ticksAtSamePos = 0;
            lastPos = worker.getPosition();
        }

        if (building instanceof AbstractBuildingGuards)
        {
            if (currentPathTarget == null
                  || BlockPosUtil.getDistance2D(building.getColony().getCenter(), currentPathTarget)
                       > Configurations.Gameplay.workingRangeTownHall + Configurations.Gameplay.townHallPadding
                  || currentPathTarget.getY() < 2)
            {
                return getNextPatrollingTarget((AbstractBuildingGuards) building);
            }

            if (worker.isWorkerAtSiteWithMove(currentPathTarget, PATH_CLOSE)
                  || ((AbstractBuildingGuards) building).getTask().equals(AbstractBuildingGuards.Task.FOLLOW)
                  || ticksAtSamePos >= SWITCH_TARGET_AFTER_TICKS)
            {
                return getNextPatrollingTarget((AbstractBuildingGuards) building);
            }
        }

        return AIState.GUARD_SEARCH_TARGET;
    }

    /**
     * Retrieves the next patrolling target from the guard tower.
     *
     * @param building his building.
     * @return the next state to go to.
     */
    private AIState getNextPatrollingTarget(final AbstractBuildingGuards building)
    {
        if (building.shallPatrolManually() && building.getTask().equals(AbstractBuildingGuards.Task.PATROL))
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.patrolling"));

            final BlockPos pos = building.getNextPatrolTarget(currentPathTarget);
            if (pos != null)
            {
                currentPathTarget = pos;
                return AIState.GUARD_SEARCH_TARGET;
            }
        }
        else if (building.getTask().equals(AbstractBuildingGuards.Task.GUARD))
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.guarding"));

            BlockPos pos = building.getGuardPos();
            if (pos == null)
            {
                pos = building.getLocation();
            }
            currentPathTarget = pos;
            return AIState.GUARD_SEARCH_TARGET;
        }
        else if (building.getTask().equals(AbstractBuildingGuards.Task.FOLLOW))
        {
            worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.following"));

            BlockPos pos = building.getPlayerToFollow();
            if (pos == null
                  || BlockPosUtil.getDistance2D(pos, building.getColony().getCenter()) > Configurations.Gameplay.workingRangeTownHall + Configurations.Gameplay.townHallPadding)
            {
                if (pos != null)
                {
                    Log.getLogger().info(BlockPosUtil.getDistance2D(pos, building.getColony().getCenter()));
                }
                final EntityPlayer player = building.getPlayer();
                if (player != null)
                {
                    LanguageHandler.sendPlayerMessage(building.getPlayer(), "com.minecolonies.coremod.job.guard.switch");
                }
                pos = building.getLocation();
                building.setTask(AbstractBuildingGuards.Task.GUARD);
            }
            currentPathTarget = pos;
            return AIState.GUARD_SEARCH_TARGET;
        }
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.patrolling"));

        currentPathTarget = getRandomBuilding();
        return AIState.GUARD_SEARCH_TARGET;
    }

    /**
     * Gets a random building from his colony.
     *
     * @return a random blockPos.
     */
    private BlockPos getRandomBuilding()
    {
        if (worker.getColony() == null || getOwnBuilding() == null)
        {
            return worker.getPosition();
        }

        final Random random = new Random();

        int tries = 0;
        BlockPos pos = null;
        while (pos == null
                 || world.getBlockState(pos).getMaterial().isLiquid()
                 || !world.getBlockState(pos.down()).getMaterial().isSolid()
                 || (!world.isAirBlock(pos) && !world.isAirBlock(pos.up())))
        {
            final Tuple<EnumFacing, EnumFacing> direction = getRandomDirectionTuple(random);
            pos =
              new BlockPos(worker.getPosition())
                .offset(direction.getFirst(), random.nextInt(LENGTH_RANGE))
                .offset(direction.getSecond(), random.nextInt(LENGTH_RANGE))
                .up(random.nextInt(UP_DOWN_RANGE))
                .down(random.nextInt(UP_DOWN_RANGE));
            lastDirection = direction;

            if (tries >= MAX_TRIES)
            {
                return this.getOwnBuilding().getLocation();
            }

            tries++;
        }


        if (BlockPosUtil.getDistance2D(pos, this.getOwnBuilding().getLocation()) > ((AbstractBuildingGuards) getOwnBuilding()).getPatrolDistance())
        {
            return this.getOwnBuilding().getLocation();
        }
        return pos;
    }

    /**
     * Searches a random direction.
     *
     * @param random a random object.
     * @return a tuple of two directions.
     */
    private Tuple<EnumFacing, EnumFacing> getRandomDirectionTuple(final Random random)
    {
        if (lastDirection != null && random.nextInt(IN_TEN) < LAST_DIRECTION_CHANCE)
        {
            return lastDirection;
        }

        return new Tuple<>(EnumFacing.random(random), EnumFacing.random(random));
    }

    /**
     * Checks if the the worker is too far from his patrol/guard/follow target.
     *
     * @param target an attack target.
     * @param range  the range allowed to be from the patrol/guard/follow target.
     * @return true if too far.
     */
    public boolean shouldReturnToTarget(final BlockPos target, final double range)
    {
        final AbstractBuilding building = getOwnBuilding();
        if (currentPathTarget == null)
        {
            getNextPatrollingTarget((AbstractBuildingGuards) building);
        }

        return building instanceof AbstractBuildingGuards
                 && BlockPosUtil.getDistance2D(target, currentPathTarget) > ((AbstractBuildingGuards) building).getPatrolDistance() + range;
    }

    /**
     * Called when a guard killed an entity.
     *
     * @param killedEntity the entity being killed.
     */
    protected void onKilledEntity(final EntityLivingBase killedEntity)
    {
        final Colony colony = this.getOwnBuilding().getColony();
        colony.getStatsManager().incrementStatistic("mobs");
        this.incrementActionsDone();
        worker.getNavigator().clearPathEntity();
    }

    /**
     * Checks if the guard found items on the ground,
     * if yes collect them, if not search for them.
     *
     * @return GUARD_GATHERING as long as gathering takes.
     */
    private AIState gathering()
    {
        worker.setLatestStatus(new TextComponentTranslation("com.minecolonies.coremod.status.gathering"));

        if (getItemsForPickUp() == null)
        {
            fillItemsList();
        }

        if (getItemsForPickUp() != null && !getItemsForPickUp().isEmpty())
        {
            gatherItems();
            return getState();
        }
        resetGatheringItems();
        return GUARD_PATROL;
    }
}
