package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.stream.Collectors;

public class EntityAICitizenAvoidEntity extends EntityAIBase
{

    /** The entity we are attached to */
    private EntityCitizen theEntity;
    private double        farSpeed;
    private double        nearSpeed;
    private Entity        closestLivingEntity;
    private float         distanceFromEntity;
    private Class         targetEntityClass;

    public EntityAICitizenAvoidEntity(EntityCitizen entity, Class targetEntityClass, float distanceFromEntity, double farSpeed, double nearSpeed)
    {
        this.theEntity = entity;
        this.targetEntityClass = targetEntityClass;
        this.distanceFromEntity = distanceFromEntity;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution of avoiding
     */
    @Override
    public boolean shouldExecute()
    {
        closestLivingEntity = getClosestToAvoid();
        return closestLivingEntity != null;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        return !theEntity.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        performMoveAway();
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        closestLivingEntity = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
        Entity newClosest = getClosestToAvoid();
        if (newClosest != null && newClosest != closestLivingEntity)
        {
            closestLivingEntity = newClosest;
            performMoveAway();
            return;
        }

        if (theEntity.getDistanceSqToEntity(closestLivingEntity) < 49.0D)
        {
            theEntity.getNavigator().setSpeed(nearSpeed);
        }
        else
        {
            theEntity.getNavigator().setSpeed(farSpeed);
        }
    }

    /**
     * Returns the closest entity to avoid
     * //TODO is this what we want? do we want to get the closest entity, and run away from that, or from enemies?
     *
     * @return  Entity to avoid
     */
    private Entity getClosestToAvoid()
    {
        if (targetEntityClass == EntityPlayer.class)
        {
            return theEntity.worldObj.getClosestPlayerToEntity(theEntity, (double) distanceFromEntity);
        }
        else
        {
            List<Entity> list = theEntity.worldObj.getEntitiesInAABBexcluding(
                    theEntity, theEntity.getEntityBoundingBox().expand((double)distanceFromEntity, 3.0D, (double)distanceFromEntity),
                    ( target) -> target.isEntityAlive() && EntityAICitizenAvoidEntity.this.theEntity.getEntitySenses().canSee(target));

            list = list.stream().filter(entity -> targetEntityClass.isInstance(entity)).collect(Collectors.toList());

            if (list.isEmpty())
            {
                return null;
            }


            return list.get(0);
        }
    }

    /**
     * Makes entity move away from {@link #closestLivingEntity}
     */
    private void performMoveAway()
    {
        theEntity.getNavigator().moveAwayFromEntityLiving(closestLivingEntity, distanceFromEntity * 2, nearSpeed);
    }
}