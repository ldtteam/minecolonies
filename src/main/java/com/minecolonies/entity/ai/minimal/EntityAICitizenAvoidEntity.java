package com.minecolonies.entity.ai.minimal;

import com.minecolonies.colony.jobs.JobGuard;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * AI task to avoid an Entity class.
 */
public class EntityAICitizenAvoidEntity extends EntityAIBase
{
    /**
     * The entity we are attached to
     */
    private EntityCitizen theEntity;
    private double        farSpeed;
    private double        nearSpeed;
    @Nullable
    private Entity        closestLivingEntity;
    private float         distanceFromEntity;
    private Class         targetEntityClass;

    /**
     * Constructor.
     *
     * @param entity             current entity.
     * @param targetEntityClass  entity class we want to avoid.
     * @param distanceFromEntity how far we want to stay away.
     * @param farSpeed           how fast we should move when we are far away.
     * @param nearSpeed          how fast we should move when we are close.
     */
    public EntityAICitizenAvoidEntity(EntityCitizen entity, Class targetEntityClass, float distanceFromEntity, double farSpeed, double nearSpeed)
    {
        this.theEntity = entity;
        this.targetEntityClass = targetEntityClass;
        this.distanceFromEntity = distanceFromEntity;
        this.farSpeed = farSpeed;
        this.nearSpeed = nearSpeed;
        super.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution of avoiding
     */
    @Override
    public boolean shouldExecute()
    {
        closestLivingEntity = getClosestToAvoid();
        return closestLivingEntity != null && !(theEntity.getColonyJob() instanceof JobGuard);
    }

    /**
     * Returns the closest entity to avoid
     *
     * @return Entity to avoid
     */
    private Entity getClosestToAvoid()
    {
        if (targetEntityClass == EntityPlayer.class)
        {
            return theEntity.worldObj.getClosestPlayerToEntity(theEntity, (double) distanceFromEntity);
        }
        else
        {
            Optional<Entity> entityOptional = theEntity.worldObj.getEntitiesInAABBexcluding(
              theEntity,
              theEntity.getEntityBoundingBox().expand(
                (double) distanceFromEntity,
                3.0D,
                (double) distanceFromEntity),
              target -> target.isEntityAlive() && EntityAICitizenAvoidEntity.this.theEntity.getEntitySenses().canSee(target))
                                                .stream()
                                                .filter(targetEntityClass::isInstance)
                                                .findFirst();

            return entityOptional.isPresent() ? entityOptional.get() : null;
        }
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
     * Makes entity move away from {@link #closestLivingEntity}
     */
    private void performMoveAway()
    {
        theEntity.getNavigator().moveAwayFromEntityLiving(closestLivingEntity, distanceFromEntity * 2D, nearSpeed);
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
        @Nullable Entity newClosest = getClosestToAvoid();
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
}
