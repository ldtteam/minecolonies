package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingGuardTower;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Abstract class which contains all the guard basics let it be range, melee or magic.
 */
public abstract class AbstractEntityAIGuard extends AbstractEntityAISkill<JobGuard>
{
    /**
     * Worker gets this distance times building level away from his building to patrol.
     */
    public static final int PATROL_DISTANCE = 40;
    /**
     * Follow the player if farther than this.
     */
    public static final int FOLLOW_RANGE = 10;
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
    private static final int MAX_ATTACKS = 50;
    /**
     * Y range in which the guard detects other entities.
     */
    private static final double HEIGHT_DETECTION_RANGE = 10D;
    /**
     * Path that close to the patrol target.
     */
    private static final int PATH_CLOSE = 2;
    /**
     * Horizontal range in which the guard picks up items.
     */
    private static final double RANGE_HORIZONTAL_PICKUP = 20.0D;
    /**
     * Vertical range in which the guard picks up items.
     */
    private static final double RANGE_VERTICAL_PICKUP   = 2.0D;
    /**
     * Amount of ticks after which the guard stops trying to gather and tries to get onto another task.
     */
    private static final int STUCK_WAIT_TICKS = 20;
    /**
     * The amount of time to wait while walking to items.
     */
    private static final int WAIT_WHILE_WALKING = 5;
    /**
     * The range in which the guard picks up items.
     */
    private static final int ITEM_PICKUP_RANGE = 3;
    /**
     * The dump base of actions, will increase depending on level.
     */
    private static final int DUMP_BASE = 20;
    /**
     * The current target.
     */
    protected EntityLivingBase targetEntity;
    /**
     * Amount of arrows already shot or sword hits dealt.
     */
    protected int attacksExecuted       = 0;
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
    private BlockPos     currentPathTarget;
    /**
     * Containing all close entities.
     */
    private List<Entity> entityList;

    /**
     * Positions of all items that have to be collected.
     */
    @Nullable
    private List<BlockPos> items;

    /**
     * Number of ticks the guard is standing still.
     */
    private int stillTicks = 0;

    /**
     * Used to store the path index
     * to check if the guard is still walking.
     */
    private int previousIndex = 0;

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
        if (!(building instanceof BuildingGuardTower))
        {
            return true;
        }

        if (!((BuildingGuardTower) building).shallRetrieveOnLowHealth())
        {
            return false;
        }

        if (worker.getHealth() > 2)
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

                if (stack == null || stack == ItemStack.EMPTY)
                {
                    continue;
                }

                if (stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType) == null)
                {
                    final int emptySlot = worker.getInventoryCitizen().getFirstEmptyStack();

                    if (emptySlot != -1)
                    {
                        worker.getInventoryCitizen().setInventorySlotContents(emptySlot, stack);
                        chest.setInventorySlotContents(i, ItemStack.EMPTY);
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
        worker.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemStack.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
        worker.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemStack.EMPTY);

        for (int i = 0; i < worker.getInventoryCitizen().getSizeInventory(); i++)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(i);

            if (stack == null || stack.getCount() == 0 || stack == ItemStack.EMPTY)
            {
                worker.getInventoryCitizen().setInventorySlotContents(i, ItemStack.EMPTY);
                continue;
            }

            if (stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType) == ItemStack.EMPTY)
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
        if (BlockPosUtil.getDistance2D(entity.getPosition(), buildingLocation) < getPatrolDistance())
        {
            if (entity instanceof EntityPlayer)
            {
                if (worker.getColony() != null && worker.getColony().getPermissions().hasPermission((EntityPlayer) entity, Permissions.Action.GUARDS_ATTACK))
                {
                    targetEntity = (EntityLivingBase) entity;
                    worker.getNavigator().clearPathEntity();
                    return AIState.GUARD_HUNT_DOWN_TARGET;
                }
                entityList.remove(0);
                setDelay(BASE_DELAY);
                return AIState.GUARD_GET_TARGET;
            }
            else if (worker.getEntitySenses().canSee(entity) && (entityList.get(0)).isEntityAlive())
            {
                worker.getNavigator().clearPathEntity();
                targetEntity = (EntityLivingBase) entity;
                return AIState.GUARD_HUNT_DOWN_TARGET;
            }
        }

        entityList.remove(0);
        setDelay(BASE_DELAY);
        return AIState.GUARD_GET_TARGET;
    }

    /**
     * Getter for the patrol distance the guard currently has.
     *
     * @return the distance in whole numbers.
     */
    private int getPatrolDistance()
    {
        return this.getOwnBuilding().getBuildingLevel() * PATROL_DISTANCE;
    }

    /**
     * Searches for the next target.
     *
     * @return the next AIState.
     */
    protected AIState searchTarget()
    {
        entityList = this.worker.world.getEntitiesWithinAABB(EntityMob.class, this.getTargetableArea(currentSearchDistance));
        entityList.addAll(this.worker.world.getEntitiesWithinAABB(EntitySlime.class, this.getTargetableArea(currentSearchDistance)));
        entityList.addAll(this.worker.world.getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(currentSearchDistance)));

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
        final BuildingGuardTower guardTower = (BuildingGuardTower) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ATTACK_DISTANCE + guardTower.getBonusVision());
    }

    /**
     * Getter calculating how many arrows the guard may shoot or deal sword hits until restock.
     *
     * @return the amount.
     */
    protected int getMaxAttacksUntilRestock()
    {
        final BuildingGuardTower guardTower = (BuildingGuardTower) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ATTACKS + guardTower.getBuildingLevel());
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

        if (building instanceof BuildingGuardTower)
        {
            if (currentPathTarget == null
                  || BlockPosUtil.getDistance2D(building.getColony().getCenter(), currentPathTarget) > Configurations.workingRangeTownHall + Configurations.townHallPadding
                  || currentPathTarget.getY() < 2)
            {
                return getNextPatrollingTarget((BuildingGuardTower) building);
            }

            if (worker.isWorkerAtSiteWithMove(currentPathTarget, PATH_CLOSE) || ((BuildingGuardTower) building).getTask().equals(BuildingGuardTower.Task.FOLLOW))
            {
                return getNextPatrollingTarget((BuildingGuardTower) building);
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
    private AIState getNextPatrollingTarget(final BuildingGuardTower building)
    {
        if (building.shallPatrolManually() && building.getTask().equals(BuildingGuardTower.Task.PATROL))
        {
            final BlockPos pos = building.getNextPatrolTarget(currentPathTarget);
            if (pos != null)
            {
                currentPathTarget = pos;
                return AIState.GUARD_SEARCH_TARGET;
            }
        }
        else if (building.getTask().equals(BuildingGuardTower.Task.GUARD))
        {
            BlockPos pos = building.getGuardPos();
            if (pos == null)
            {
                pos = building.getLocation();
            }
            currentPathTarget = pos;
            return AIState.GUARD_SEARCH_TARGET;
        }
        else if (building.getTask().equals(BuildingGuardTower.Task.FOLLOW))
        {
            BlockPos pos = building.getPlayerToFollow();
            if (pos == null || BlockPosUtil.getDistance2D(pos, building.getColony().getCenter()) > Configurations.workingRangeTownHall + Configurations.townHallPadding)
            {
                if (pos != null)
                {
                    Log.getLogger().info(BlockPosUtil.getDistance2D(pos, building.getColony().getCenter()));
                }
                final EntityPlayer player = building.getPlayer();
                if (player != null)
                {
                    LanguageHandler.sendPlayerMessage(building.getPlayer(), LanguageHandler.format("com.minecolonies.coremod.job.guard.switch"));
                }
                pos = building.getLocation();
                building.setTask(BuildingGuardTower.Task.GUARD);
            }
            currentPathTarget = pos;
            return AIState.GUARD_SEARCH_TARGET;
        }
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

        final Collection<AbstractBuilding> buildingList = worker.getColony().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();

        final int random = worker.getRandom().nextInt(buildingArray.length);
        final AbstractBuilding building = (AbstractBuilding) buildingArray[random];

        if (building instanceof BuildingGuardTower
              || BlockPosUtil.getDistance2D(building.getLocation(), this.getOwnBuilding().getLocation()) > getPatrolDistance())
        {
            return this.getOwnBuilding().getLocation();
        }

        return building.getLocation();
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

        if (currentPathTarget == null && building instanceof BuildingGuardTower)
        {
            getNextPatrollingTarget((BuildingGuardTower) building);
        }

        if (building instanceof BuildingGuardTower && BlockPosUtil.getDistance2D(target, currentPathTarget) > range)
        {
            return true;
        }
        return false;
    }

    /**
     * Called when a guard killed an entity.
     *
     * @param killedEntity the entity being killed.
     */
    protected void onKilledEntity(final EntityLivingBase killedEntity)
    {
        final Colony colony = this.getOwnBuilding().getColony();
        colony.incrementMobsKilled();
        incrementActionsDone();
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
        if (items == null)
        {
            searchForItems();
        }
        if (!items.isEmpty())
        {
            gatherItems();
            return getState();
        }
        items = null;
        return GUARD_PATROL;
    }

    /**
     * Search for all items around the Guard.
     * and store them in the items list
     */
    private void searchForItems()
    {
        items = new ArrayList<>();

        items = world.getEntitiesWithinAABB(EntityItem.class, worker.getEntityBoundingBox().expand(RANGE_HORIZONTAL_PICKUP, RANGE_VERTICAL_PICKUP, RANGE_HORIZONTAL_PICKUP))
                  .stream()
                  .filter(item -> item != null && !item.isDead)
                  .map(BlockPosUtil::fromEntity)
                  .collect(Collectors.toList());
    }

    /**
     * Collect one item by walking to it.
     */
    private void gatherItems()
    {
        worker.setCanPickUpLoot(true);
        if (worker.getNavigator().noPath())
        {
            final BlockPos pos = getAndRemoveClosestItem();
            worker.isWorkerAtSiteWithMove(pos, ITEM_PICKUP_RANGE);
            return;
        }
        if (worker.getNavigator().getPath() == null)
        {
            setDelay(WAIT_WHILE_WALKING);
            return;
        }

        final int currentIndex = worker.getNavigator().getPath().getCurrentPathIndex();
        //We moved a bit, not stuck
        if (currentIndex != previousIndex)
        {
            stillTicks = 0;
            previousIndex = currentIndex;
            return;
        }

        stillTicks++;
        //Stuck for too long
        if (stillTicks > STUCK_WAIT_TICKS)
        {
            //Skip this item
            worker.getNavigator().clearPathEntity();
        }
    }

    /**
     * Find the closest item and remove it from the list.
     *
     * @return the closest item
     */
    private BlockPos getAndRemoveClosestItem()
    {
        if (items == null)
        {
            return worker.getPosition();
        }

        int index = 0;
        double distance = Double.MAX_VALUE;

        for (int i = 0; i < items.size(); i++)
        {
            final double tempDistance = items.get(i).distanceSq(worker.getPosition());
            if (tempDistance < distance)
            {
                index = i;
                distance = tempDistance;
            }
        }

        return items.remove(index);
    }
}
