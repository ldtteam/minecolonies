package com.minecolonies.entity.ai;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.util.Utils;
import com.minecolonies.util.Vec3Utils;
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
            deliveryman.setDestination(deliveryman.getTownHall().getDeliverymanRequired().get(0));
        }
        Vec3Utils.tryMoveLivingToXYZ(deliveryman, deliveryman.getDestination());
    }

    @Override
    public void updateTask()
    {
        if(!Vec3Utils.isWorkerAtSite(deliveryman, deliveryman.getDestination()))
        {
            if(deliveryman.getNavigator().noPath())
            {
                if(!Vec3Utils.tryMoveLivingToXYZ(deliveryman, deliveryman.getDestination()))
                {
                    deliveryman.setStatus(PATHFINDING_ERROR);
                }
            }
            return;
        }

        deliveryman.setStatus(WORKING);

        TileEntityHutWorker workHut = (TileEntityHutWorker) Vec3Utils.getTileEntityFromVec(world, deliveryman.getDestination());
        EntityWorker worker = (EntityWorker) Utils.getEntityFromUUID(world, workHut.getWorkerID());
        for(int i = 0; i < worker.getItemsNeeded().size(); i++)
        {
            ItemStack itemstack = worker.getItemsNeeded().get(i);
            int amount = itemstack.stackSize;
            for(int j = 0; j < workHut.getSizeInventory(); j++)
            {
                ItemStack hutItem = workHut.getStackInSlot(j);
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
                workHut.setStackInInventory(new ItemStack(itemstack.getItem(), amount, itemstack.getItemDamage()));
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
