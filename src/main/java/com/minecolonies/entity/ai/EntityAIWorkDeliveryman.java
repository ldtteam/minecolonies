package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.item.ItemStack;

import static com.minecolonies.entity.EntityCitizen.Status.*;

/**
 * Performs deliveryman work
 * Created: July 18, 2014
 *
 * @author MrIbby
 */
public class EntityAIWorkDeliveryman extends EntityAIWork
{
    private final EntityDeliveryman deliveryman;

    public EntityAIWorkDeliveryman(EntityDeliveryman deliveryman)
    {
        super(deliveryman);
        this.deliveryman = deliveryman;
    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute() && (deliveryman.hasDestination() || deliveryman.isNeeded());
    }

    @Override
    public void startExecuting()
    {
        if(!deliveryman.hasDestination())
        {
            deliveryman.setDestination(deliveryman.getColony().getDeliverymanRequired().get(0));
        }
        ChunkCoordUtils.tryMoveLivingToXYZ(deliveryman, deliveryman.getDestination());
    }

    @Override
    public void updateTask()
    {
        if(!ChunkCoordUtils.isWorkerAtSiteWithMove(deliveryman, deliveryman.getDestination()))
        {
            return;
        }

        deliveryman.setStatus(WORKING);

        //  TODO - Actually know the Building, not the ID of it
        Building destinationBuilding = deliveryman.getColony().getBuilding(deliveryman.getDestination());

        if (destinationBuilding == null ||
                !(destinationBuilding instanceof BuildingWorker))
        {
            return;
        }

        EntityWorker worker = (EntityWorker)deliveryman.getColony().getCitizen(((BuildingWorker)destinationBuilding).getWorkerId());
        TileEntityColonyBuilding destinationTileEntity = destinationBuilding.getTileEntity();

        if (worker == null || destinationTileEntity == null)
        {
            //  The recipient or their building's TE aren't loaded currently.  Maybe do something else?
            return;
        }

        for(int i = 0; i < worker.getItemsNeeded().size(); i++)
        {
            ItemStack itemstack = worker.getItemsNeeded().get(i);
            int amount = itemstack.stackSize;
            for(int j = 0; j < destinationTileEntity.getSizeInventory(); j++)
            {
                ItemStack hutItem = destinationTileEntity.getStackInSlot(j);
                if(hutItem != null && hutItem.isItemEqual(itemstack))
                {
                    amount -= hutItem.stackSize;
                    if(amount <= 0) break;
                }
            }
            if(amount > 0)
            {
                if(!Configurations.deliverymanInfiniteResources)
                {
                    //TODO: resource handling
                }
                InventoryUtils.setStack(destinationTileEntity, new ItemStack(itemstack.getItem(), amount, itemstack.getItemDamage()));
            }
            worker.getItemsNeeded().remove(i);
            i--;
        }

        deliveryman.setDestination(null);
        resetTask();
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting() && deliveryman.hasDestination();
    }
}
