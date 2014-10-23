package com.minecolonies.entity.ai;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.item.ItemStack;

import java.util.UUID;

import static com.minecolonies.entity.EntityCitizen.Status.*;

/**
 * Performs deliveryman work
 * Created: July 18, 2014
 *
 * @author MrIbby
 */
public class EntityAIWorkDeliveryman extends EntityAIWork<EntityDeliveryman>
{
    public EntityAIWorkDeliveryman(EntityDeliveryman deliveryman)
    {
        super(deliveryman);
    }

    @Override
    public boolean shouldExecute()
    {
        return super.shouldExecute() && (worker.hasDestination() || worker.isNeeded());
    }

    @Override
    public void startExecuting()
    {
        if(!worker.hasDestination())
        {
            worker.setDestination(worker.getColony().getDeliverymanRequired().get(0));
        }
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, worker.getDestination());
    }

    @Override
    public void updateTask()
    {
        if(!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, worker.getDestination()))
        {
            return;
        }

        worker.setStatus(WORKING);

        //  TODO - Actually know the Building, not the ID of it
        Building destinationBuilding = worker.getColony().getBuilding(worker.getDestination());

        if (!(destinationBuilding instanceof BuildingWorker))
        {
            return;
        }

        CitizenData targetCitizen = ((BuildingWorker)destinationBuilding).getWorker();
        if (targetCitizen == null)
        {
            return;
        }

        EntityCitizen targetCitizenEntity = targetCitizen.getCitizenEntity();
        if (!(targetCitizenEntity instanceof EntityWorker))
        {
            return;
        }

        EntityWorker target = (EntityWorker)targetCitizenEntity;

        TileEntityColonyBuilding destinationTileEntity = destinationBuilding.getTileEntity();
        if (target == null || destinationTileEntity == null)
        {
            //  The recipient or their building's TE aren't loaded currently.  Maybe do something else?
            return;
        }

        for(int i = 0; i < target.getItemsNeeded().size(); i++)
        {
            ItemStack itemstack = target.getItemsNeeded().get(i);
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
            target.getItemsNeeded().remove(i);
            i--;
        }

        worker.setDestination(null);
        resetTask();
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting() && worker.hasDestination();
    }
}
