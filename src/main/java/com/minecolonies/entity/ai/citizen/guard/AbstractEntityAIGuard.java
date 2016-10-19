package com.minecolonies.entity.ai.citizen.guard;

import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.colony.buildings.BuildingGuardTower;
import com.minecolonies.colony.jobs.JobGuard;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Abstract class which contains all the guard basics let it be range, melee or magic.
 */
public abstract class AbstractEntityAIGuard extends AbstractEntityAISkill<JobGuard>
{
    /**
     * The start search distance of the guard to track/attack entities may get more depending on the level.
     */
    private static final double MAX_ATTACK_DISTANCE = 20.0D;

    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 100;

    /**
     * Distance the guard starts searching.
     */
    private static final int START_SEARCH_DISTANCE = 5;

    /**
     * Basic delay after operations.
     */
    private static final int BASE_DELAY = 10;

    /**
     * Max amount the guard can shoot arrows before restocking.
     */
    private static final int MAX_ARROWS_SHOT = 50;

    /**
     * Y range in which the guard detects other entities.
     */
    private static final double HEIGHT_DETECTION_RANGE = 10D;

    /**
     * The distance the guard is searching entities in currently.
     */
    private int currentSearchDistance = START_SEARCH_DISTANCE;

    /**
     * The current target.
     */
    protected EntityLivingBase targetEntity;

    /**
     * Current goTo task.
     */
    private BlockPos currentPatrolTarget;

    /**
     * Containing all close entities.
     */
    private List<Entity> entityList;

    /**
     * Amount of arrows already shot.
     */
    protected int arrowsShot = 0;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    protected AbstractEntityAIGuard(@NotNull final JobGuard job)
    {
        super(job);
    }

    /**
     * Goes back to the building and tries to take armour from it when he hasn't in his inventory.
     * @return
     */
    protected AIState goToBuilding()
    {
        if(!walkToBuilding())
        {
            final AbstractBuildingWorker workBuilding = getOwnBuilding();
            if(workBuilding != null)
            {
                final TileEntityColonyBuilding chest = workBuilding.getTileEntity();
                for (int i = 0; i < workBuilding.getTileEntity().getSizeInventory(); i++)
                {
                    final ItemStack stack = chest.getStackInSlot(i);

                    if (stack == null)
                    {
                        continue;
                    }

                    if (stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType) == null)
                    {
                        final int emptySlot = worker.getInventoryCitizen().getFirstEmptySlot();
                        worker.getInventoryCitizen().setInventorySlotContents(emptySlot, stack);
                        chest.setInventorySlotContents(i, null);
                    }
                }
            }

            arrowsShot = 0;
            return AIState.START_WORKING;
        }
        return AIState.GUARD_RESTOCK;
    }

    /**
     * Updates the equipment. Always take the first item of each type and set it.
     */
    protected void updateArmor()
    {
        worker.setItemStackToSlot(EntityEquipmentSlot.CHEST, null);
        worker.setItemStackToSlot(EntityEquipmentSlot.FEET, null);
        worker.setItemStackToSlot(EntityEquipmentSlot.HEAD, null);
        worker.setItemStackToSlot(EntityEquipmentSlot.LEGS, null);

        for(int i = 0; i < worker.getInventoryCitizen().getSizeInventory(); i++)
        {
            ItemStack stack = worker.getInventoryCitizen().getStackInSlot(i);

            if(stack == null)
            {
                continue;
            }

            if(stack.stackSize == 0)
            {
                worker.getInventoryCitizen().setInventorySlotContents(i, null);
                continue;
            }

            if(stack.getItem() instanceof ItemArmor && worker.getItemStackFromSlot(((ItemArmor) stack.getItem()).armorType)== null)
            {
                worker.setItemStackToSlot(((ItemArmor) stack.getItem()).armorType, stack);
            }
        }
    }

    /**
     * Chooses a target from the list.
     * @return the next state.
     */
    protected AIState getTarget()
    {
        if(entityList.isEmpty())
        {
            return AIState.GUARD_PATROL;
        }

        if(entityList.get(0) instanceof EntityPlayer)
        {
            if(worker.getColony() != null && worker.getColony().getPermissions().getRank((EntityPlayer) entityList.get(0)) == Permissions.Rank.HOSTILE)
            {
                targetEntity = (EntityLivingBase) entityList.get(0);
                worker.getNavigator().clearPathEntity();
                return AIState.GUARD_HUNT_DOWN_TARGET;
            }
            entityList.remove(0);
            setDelay(BASE_DELAY);
            return AIState.GUARD_GET_TARGET;
        }
        else if (!worker.getEntitySenses().canSee(entityList.get(0)) || !(entityList.get(0)).isEntityAlive())
        {
            entityList.remove(0);
            setDelay(BASE_DELAY);
            return AIState.GUARD_GET_TARGET;
        }
        else
        {
            worker.getNavigator().clearPathEntity();
            targetEntity = (EntityLivingBase) entityList.get(0);
            return AIState.GUARD_HUNT_DOWN_TARGET;
        }

    }

    /**
     * Searches for the next taget.
     * @return the next AIState.
     */
    protected AIState searchTarget()
    {
        entityList = this.worker.worldObj.getEntitiesWithinAABB(EntityMob.class, this.getTargetableArea(currentSearchDistance));
        entityList.addAll(this.worker.worldObj.getEntitiesWithinAABB(EntitySlime.class, this.getTargetableArea(currentSearchDistance)));
        entityList.addAll(this.worker.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(currentSearchDistance)));

        if(checkOrRequestItems(new ItemStack(Items.BOW)))
        {
            return AIState.GUARD_SEARCH_TARGET;
        }
        worker.setHeldItem(worker.findFirstSlotInInventoryWith(Items.BOW));

        setDelay(BASE_DELAY);
        if(entityList.isEmpty())
        {
            if(currentSearchDistance < getMaxVision())
            {
                currentSearchDistance += START_SEARCH_DISTANCE;
            }
            else
            {
                currentSearchDistance = START_SEARCH_DISTANCE;
                Log.getLogger().info("Patroll! in searchTarget");
                return AIState.GUARD_PATROL;
            }

            return AIState.GUARD_SEARCH_TARGET;
        }
        return AIState.GUARD_GET_TARGET;
    }

    /**
     * Getter for the vision or attack distance.
     * @return the max vision.
     */
    private double getMaxVision()
    {
        BuildingGuardTower guardTower = (BuildingGuardTower) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ATTACK_DISTANCE + guardTower.getBonusVision());
    }

    /**
     * Getter calculating how many arrows the guard may shoot until restock.
     * @return the amount.
     */
    protected int getMaxArrowsShot()
    {
        BuildingGuardTower guardTower = (BuildingGuardTower) worker.getWorkBuilding();
        return (guardTower == null) ? 0 : (MAX_ARROWS_SHOT + guardTower.getBuildingLevel());
    }



    /**
     * Lets the guard patrol inside the colony area searching for mobs.
     * @return the next state to go.
     */
    protected AIState patrol()
    {
        worker.setAIMoveSpeed(1);

        if(currentPatrolTarget == null)
        {
            currentPatrolTarget = getRandomBuilding();
        }

        if(worker.isWorkerAtSiteWithMove(currentPatrolTarget, 3))
        {
            currentPatrolTarget = null;
        }

        return AIState.GUARD_SEARCH_TARGET;
    }

    /**
     * Gets a random building from his colony.
     * @return a random blockPos.
     */
    private BlockPos getRandomBuilding()
    {
        if(worker.getColony() == null)
        {
            return worker.getPosition();
        }

        Map<BlockPos, AbstractBuilding> buildingMap = worker.getColony().getBuildings();
        int random = worker.getRandom().nextInt(buildingMap.size());
        return (BlockPos) worker.getColony().getBuildings().keySet().toArray()[random];
    }



    private AxisAlignedBB getTargetableArea(double range)
    {
        return this.worker.getEntityBoundingBox().expand(range, HEIGHT_DETECTION_RANGE, range);
    }





}
