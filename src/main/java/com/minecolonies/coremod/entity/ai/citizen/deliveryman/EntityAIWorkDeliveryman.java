package com.minecolonies.coremod.entity.ai.citizen.deliveryman;

import com.minecolonies.blockout.Log;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

        Colony colony = getOwnBuilding().getColony();
        if(colony != null)
        {
            AbstractBuilding building = colony.getBuilding(gatherTarget);
            gatherFromBuilding(building);
            return DUMPING;
        }
        return START_WORKING;
    }

    /**
     * Gather not needed Items from building.
     * @param building building to gather it from.
     */
    private void gatherFromBuilding(final AbstractBuilding building)
    {
        for(int i = 0 ; i < building.getTileEntity().getSizeInventory(); i++)
        {
            ItemStack stack = building.getTileEntity().getStackInSlot(i);

            boolean needsItem = false;
            if(building instanceof AbstractBuildingWorker && ((AbstractBuildingWorker) building).neededForWorker(stack))
            {
                needsItem = true;
            }
            //todo count how much of the itemStack has to be left behind.
            for(Map.Entry<ItemStorage, Integer> entry: building.needXForWorker().entrySet())
            {
                if(entry.getKey().getItem() == stack.getItem() && entry.getKey().getDamageValue() == stack.getItemDamage())
                {
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
        AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if(ownBuilding instanceof BuildingDeliveryman)
        {
            AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
            if (buildingToDeliver != null)
            {
                if(!worker.isWorkerAtSiteWithMove(buildingToDeliver.getLocation(), MIN_DISTANCE_TO_WAREHOUSE))
                {
                    return DELIVERY;
                }

                InventoryCitizen workerInventory  = worker.getInventoryCitizen();
                for(int i = 0; i < workerInventory.getSizeInventory(); i++)
                {
                    ItemStack stack = workerInventory.getStackInSlot(i);
                    if(stack == null)
                    {
                        continue;
                    }

                    if(InventoryUtils.addItemStackToInventory(buildingToDeliver.getTileEntity(), stack))
                    {
                        workerInventory.removeStackFromSlot(i);
                    }
                }
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
        AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if(ownBuilding instanceof BuildingDeliveryman)
        {
            AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
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
        AbstractBuildingWorker ownBuilding = getOwnBuilding();
        if (ownBuilding instanceof BuildingDeliveryman)
        {
            AbstractBuilding buildingToDeliver = ((BuildingDeliveryman) ownBuilding).getBuildingToDeliver();
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
    private boolean hasTools(final AbstractBuilding buildingToDeliver)
    {
        String requiredTool = buildingToDeliver.getRequiredTool();
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
     * Gather item
     * @param buildingToDeliver
     */
    private void gatherItems(final AbstractBuilding buildingToDeliver)
    {
        BlockPos position;
        if(itemsToDeliver.isEmpty())
        {
            String tool = buildingToDeliver.getRequiredTool();
            position = wareHouse.getTileEntity().getPositionOfChestWithTool(tool, Utils.PICKAXE.equals(tool) ? buildingToDeliver.getNeededPickaxeLevel() : -1);
        }
        else
        {
            //todo take at least the amount of items he needs.
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

        TileEntity tileEntity = world.getTileEntity(position);
        if(tileEntity instanceof TileEntityChest)
        {
            if (itemsToDeliver.isEmpty())
            {
                String tool = buildingToDeliver.getRequiredTool();
                isToolInTileEntity((TileEntityChest) tileEntity, tool);
            }
            else
            {
                ItemStack stack = itemsToDeliver.get(0);
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

        AbstractBuildingWorker ownBuilding = getOwnBuilding();
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
        if(wareHouse != null && wareHouse.getTileEntity() != null)
        {
            return false;
        }

        Map<BlockPos, AbstractBuilding> buildings = job.getColony().getBuildings();
        for(AbstractBuilding building: buildings.values())
        {
            if(building instanceof BuildingWareHouse && ((BuildingWareHouse) building).registerWithWareHouse(this.getOwnBuilding()))
            {
                wareHouse = (BuildingWareHouse) building;
                return false;
            }
        }

        //todo tell player that deliveryman can't find a warehouse.
        Log.getLogger().info("Unable to find wareHouse");
        return true;
    }
}
