package com.minecolonies.entity.ai;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import com.minecolonies.entity.EntityWorker;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.util.Utils;
import com.minecolonies.util.Vec3Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Performs deliveryman work
 * Created: July 18, 2014
 *
 * @author MrIbby
 */
public class EntityAIWorkDeliveryman extends EntityAIBase
{
    private EntityDeliveryman deliveryman;
    private World             world;

    public EntityAIWorkDeliveryman(EntityDeliveryman deliveryman)
    {
        setMutexBits(3);
        this.deliveryman = deliveryman;
        this.world = deliveryman.worldObj;
    }

    @Override
    public boolean shouldExecute()
    {
        return deliveryman.isWorkTime() && (deliveryman.hasDestination() || (deliveryman.isNeeded() && getDeliverymanRequired() != null));
    }

    @Override
    public void startExecuting()
    {
        if(!deliveryman.hasDestination())
        {
            deliveryman.setDestination(getDeliverymanRequired());
        }
        Vec3 destination = deliveryman.getDestination();
        deliveryman.getNavigator().tryMoveToXYZ(destination.xCoord, destination.yCoord, destination.zCoord, 1.0F);
    }

    @Override
    public void updateTask()
    {
        if(!continueExecuting())
            return;

        deliveryman.setStatus(EntityDeliveryman.Status.WORKING);

        if(!Vec3Utils.isWorkerAtSite(deliveryman, deliveryman.getDestination())) return;

        TileEntityHutWorker workHut = (TileEntityHutWorker) Vec3Utils.getTileEntityFromVec(world, deliveryman.getDestination());
        EntityWorker worker = (EntityWorker) Utils.getEntityFromUUID(world, workHut.getWorkerID());
        for(int i = 0; i < worker.getItemsNeeded().size(); i++)
        {
            ItemStack itemstack = worker.getItemsNeeded().get(i);
            if(!Configurations.deliverymanInfiniteResources)
            {
                //TODO: resource handling
            }
            workHut.setStackInInventory(itemstack);
            worker.getItemsNeeded().remove(i);
            i--;
        }

        deliveryman.setDestination(null);
        deliveryman.setStatus(EntityDeliveryman.Status.IDLE);
    }

    @Override
    public boolean continueExecuting()
    {
        return deliveryman.isWorkTime() && deliveryman.hasDestination();
    }

    private Vec3 getDeliverymanRequired()
    {
        for(Entity entity : Utils.getEntitiesFromUUID(world, deliveryman.getTownHall().getCitizens()))
        {
            if(entity instanceof EntityWorker)
            {
                EntityWorker worker = (EntityWorker) entity;
                if(worker.getWorkHut() != null && !worker.getItemsNeeded().isEmpty())
                {
                    return worker.getWorkHut().getPosition();
                }
            }
        }
        return null;
    }
}
