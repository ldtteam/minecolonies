package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.item.handling.ItemStorage;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Performs deliveryman work.
 * Created: July 18, 2014
 *
 * @author MrIbby
 */
public class EntityAIWorkDeliveryman extends AbstractEntityAIInteract<JobDeliveryman>
{
    /**
     * Min distance the worker should have to the warehouse to make any decisions.
     */
    private static final int MIN_DISTANCE_TO_WAREHOUSE = 5;

    /**
     * Walking speed double at this level.
     */
    private static final double WALKING_SPEED_MULTIPLIER  = 25;

    /**
     * Warehouse the deliveryman is assigned to.
     */
    private BuildingWareHouse wareHouse = null;

    /**
     * List of itemStacks the deliveryman will deliver to the requesting building.
     */
    private List<ItemStack> itemsToDeliver = new ArrayList<>();

    /**
     * Next target the deliveryman should gather stuff at.
     */
    private BlockPos gatherTarget = null;

    /**
     * Initialize the deliveryman and add all his tasks.
     *
     * @param deliveryman the job he has.
     */
    public EntityAIWorkDeliveryman(@NotNull final JobDeliveryman deliveryman)
    {
        super(deliveryman);
        super.registerTargets(
                /**
                 * Check if tasks should be executed.
                 */
                new AITarget(this::checkIfExecute, IDLE),
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::checkWareHouse),
                new AITarget(PREPARE_DELIVERY, this::prepareDelivery),
                new AITarget(GATHER_IN_WAREHOUSE, this::gatherItemsFromWareHouse),
                new AITarget(DELIVERY, this::deliver),
                new AITarget(GATHERING, this::gather),
                new AITarget(DUMPING, this::dump)

        );
        worker.setSkillModifier(2 * worker.getCitizenData().getEndurance() + worker.getCitizenData().getCharisma());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Gather items from a random hut which the hut doesn't need.
     * @return the next state to go to.
     */
    public AIState gather()
    {
        if(gatherTarget == null)
        {
            gatherTarget = getRandomBuilding();
        }

        if(gatherTarget == null)
        {
            return GATHERING;
        }

        if(!worker.isWorkerAtSiteWithMove(gatherTarget, MIN_DISTANCE_TO_WAREHOUSE))
        {
            return GATHERING;
        }

        final Colony colony = getOwnBuilding().getColony();
        if(colony != null)
        {
            final AbstractBuilding building = colony.getBuilding(gatherTarget);
            gatherFromBuilding(building);
            return DUMPING;
        }
        return START_WORKING;
    }

    /**
     * Gather not needed Items from building.
     * @param building building to gather it from.
     */
    private void gatherFromBuilding(@NotNull final AbstractBuilding building)
    {
        final List<ItemStorage> alreadyKept = new ArrayList<>();
        for(int i = 0 ; i < building.getTileEntity().getSizeInventory(); i++)
        {
            final ItemStack stack = building.getTileEntity().getStackInSlot(i);

            if(stack == null)
            {
                continue;
            }

            boolean needsItem = false;
            if(building instanceof AbstractBuildingWorker && ((AbstractBuildingWorker) building).neededForWorker(stack))
            {
                needsItem = true;
            }
            //Always leave one stack of the needX behind.
            for(final Map.Entry<ItemStorage, Integer> entry: building.needXForWorker().entrySet())
            {
                if(entry.getKey().getItem() == stack.getItem()
                        && entry.getKey().getDamageValue() == stack.getItemDamage()
                        && !alreadyKept.contains(entry.getKey()))
                {
                    alreadyKept.add(entry.getKey());
                    needsItem = true;
                }
            }

            if(!needsItem)
            {
                if(!worker.getInventoryCitizen().addItemStackToInventory(stack))
                {
                    return;
                }
                building.getTileEntity().removeStackFromSlot(i);
            }
        }
    }

    /**
     * Gets a random building from his colony.
     *
     * @return a random blockPos.
     */
    @Nullable
    private BlockPos getRandomBuilding()
    {
        if (worker.getColony() == null || getOwnBuilding() == null)
        {
            return null;
        }

        final Collection<AbstractBuilding> buildingList = worker.getColony().getBuildings().values();
        final Object[] buildingArray = buildingList.toArray();

        final int random = worker.getRandom().nextInt(buildingArray.length);
        final AbstractBuilding building = (AbstractBuilding) buildingArray[random];

        if (building instanceof BuildingWareHouse
                || building instanceof BuildingDeliveryman || building instanceof BuildingTownHall)
        {
            return null;
        }

        return building.getLocation();
    }

    /**
     * Dump the inventory into the warehouse.
     * @return the next state to go to.
     */
    public AIState dump()
    {
        if(!worker.isWorkerAtSiteWithMove(wareHouse.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return DUMPING;
        }

        wareHouse.getTileEntity().dumpInventoryIntoWareHouse(worker.getInventoryCitizen());
        gatherTarget = null;

        return START_WORKING;
    }


    /**
     * Deliver the items to the hut.
     * @return the next state.
     */
    private AIState deliver()
    {
        final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if(ownBuilding instanceof BuildingDeliveryman)
        {
            final AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
            if (buildingToDeliver != null)
            {
                if(!worker.isWorkerAtSiteWithMove(buildingToDeliver.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
                {
                    return DELIVERY;
                }

                final InventoryCitizen workerInventory  = worker.getInventoryCitizen();
                for(int i = 0; i < workerInventory.getSizeInventory(); i++)
                {
                   final  ItemStack stack = workerInventory.getStackInSlot(i);
                    if(stack == null)
                    {
                        continue;
                    }

                    if(InventoryUtils.addItemStackToInventory(buildingToDeliver.getTileEntity(), stack))
                    {
                        workerInventory.removeStackFromSlot(i);
                    }
                }
                worker.addExperience(1.0D);
                buildingToDeliver.setOnGoingDelivery(false);
            }
        }
        return START_WORKING;
    }

    /**
     * Prepare deliveryman for delivery.
     * Check if the building still needs the item and if the required items are still in the warehouse.
     * @return the next state to go to.
     */
    private AIState prepareDelivery()
    {
        final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if(ownBuilding instanceof BuildingDeliveryman)
        {
            final AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
            if(buildingToDeliver != null)
            {
                boolean ableToDeliver = wareHouse.getTileEntity().checkInWareHouse(buildingToDeliver);
                if(!ableToDeliver)
                {
                    return START_WORKING;
                }
                itemsToDeliver = new ArrayList<>(buildingToDeliver.getNeededItems());
                return GATHER_IN_WAREHOUSE;
            }
        }
        return START_WORKING;
    }

    private AIState gatherItemsFromWareHouse()
    {
        final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding instanceof BuildingDeliveryman)
        {
            final AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
            if (buildingToDeliver != null)
            {
                if(itemsToDeliver.isEmpty() && hasTools(buildingToDeliver))
                {
                    return DELIVERY;
                }

                gatherItems(buildingToDeliver);
                return GATHER_IN_WAREHOUSE;
            }
        }
        return START_WORKING;
    }

    /**
     * Check if the deliveryman has all the tools to make the delivery.
     * @param buildingToDeliver the building to deliver to.
     * @return true if is ready to deliver.
     */
    private boolean hasTools(@NotNull final AbstractBuilding buildingToDeliver)
    {
        final String requiredTool = buildingToDeliver.getRequiredTool();
        if(requiredTool.isEmpty())
        {
            return true;
        }

        if(requiredTool.equals(Utils.PICKAXE))
        {
            return InventoryUtils.isPickaxeInTileEntity(worker.getInventoryCitizen(), buildingToDeliver.getNeededPickaxeLevel());
        }
        return InventoryUtils.isToolInTileEntity(worker.getInventoryCitizen(), requiredTool);
    }

    /**
     * Gather item from chest.
     * Gathers only one stack of the item.
     * @param buildingToDeliver
     */
    private void gatherItems(@NotNull final AbstractBuilding buildingToDeliver)
    {
        BlockPos position;
        if(itemsToDeliver.isEmpty())
        {
            String tool = buildingToDeliver.getRequiredTool();
            position = wareHouse.getTileEntity().getPositionOfChestWithTool(tool, Utils.PICKAXE.equals(tool) ? buildingToDeliver.getNeededPickaxeLevel() : -1);
        }
        else
        {
            ItemStack stack = itemsToDeliver.get(0);
            position = wareHouse.getTileEntity().getPositionOfChestWithItemStack(stack);
        }

        if(position == null)
        {
            return;
        }

        if(!worker.isWorkerAtSiteWithMove(position, MIN_DISTANCE_TO_WAREHOUSE))
        {
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(position);
        if(tileEntity instanceof TileEntityChest)
        {
            if (itemsToDeliver.isEmpty())
            {
                isToolInTileEntity((TileEntityChest) tileEntity, buildingToDeliver.getRequiredTool());
            }
            else
            {
                final ItemStack stack = itemsToDeliver.get(0);
                if(isInTileEntity((TileEntityChest) tileEntity, stack))
                {
                    itemsToDeliver.remove(0);
                }
            }
        }
    }


    /**
     * Check the wareHouse for the next task.
     * @return the next AiState to go to.
     */
    private AIState checkWareHouse()
    {
        if(!worker.isWorkerAtSiteWithMove(wareHouse.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
        {
            return START_WORKING;
        }

        @Nullable final AbstractBuilding buildingToDeliver = wareHouse.getTileEntity().getTask();
        if(buildingToDeliver == null)
        {
            return GATHERING;
        }

        final AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if(ownBuilding instanceof BuildingDeliveryman)
        {
            ((BuildingDeliveryman) ownBuilding).setBuildingToDeliver(buildingToDeliver);
        }

        return PREPARE_DELIVERY;
    }

    /**
     * Check if the deliveryman code should be executed.
     * More concretely if he has a warehouse to work at.
     * @return false if should continue as planned.
     */
    private boolean checkIfExecute()
    {
        worker.setAIMoveSpeed((float) (worker.BASE_MOVEMENT_SPEED + worker.BASE_MOVEMENT_SPEED * worker.getLevel()/WALKING_SPEED_MULTIPLIER));

        if(wareHouse != null && wareHouse.getTileEntity() != null)
        {
            return false;
        }

        final Map<BlockPos, AbstractBuilding> buildings = job.getColony().getBuildings();
        for(final AbstractBuilding building: buildings.values())
        {
            if(building instanceof BuildingWareHouse && ((BuildingWareHouse) building).registerWithWareHouse(this.getOwnBuilding()))
            {
                wareHouse = (BuildingWareHouse) building;
                return false;
            }
        }

        worker.sendLocalizedChat("com.minecolonies.coremod.job.deliveryman.noWarehouse");
        return true;
    }
}
