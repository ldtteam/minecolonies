package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.EntityCitizen;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author kevin
 *
 */
public class EntityAIMournCitizen extends EntityAIBase
{
    /**
     * handler to the citizen thisis located
     */
    private final EntityCitizen citizen;

    /**
     * Speed at which the citizen will move around
     */
    private final double        speed;

    /**
     * X location of the next position to move to
     */
    private       double        xPosition = 0;

    /**
     * Y location of the next position to move to
     */
    private       double        yPosition = 0;

    /**
     * Z location of the next position to move to
     */
    private       double        zPosition = 0;

    /**
     * Indicated the citizen reach the mourn location
     */
    private boolean reachedFinalDestination = false;

    /**
     * Indicates the citizen is moving toward the mourn location
     */
    private boolean goingToMourn = false;

    /**
     * Max distance for the look for another citizen
     */
    private static final int maxDistanceForPlayer = 1;

    /**
     * Pointer to the closest citizen to look at.
     */
    private Entity closestEntity;

    /**
     * Length of time the citizen will mourn.
     */
    private int mourningTime;
    /**
     * Indicate to look at the citizen or not.
     */
    private boolean continueLooking = false;

    /**
     * Location of the home location
     */
    private Vec3d homeLocation;

    /**
     * Constant values of mourning
     */
    private static final int MIN_DESTINATION_TO_LOCATION = 15;
    private static final int MIN_MOURN_TIME = 2000;
    private static final int MIN_MOURN_RANDOM_TIME = 1000;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     * @param speed   the speed.
     */
    public EntityAIMournCitizen(final EntityCitizen citizen, final double speed)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;
        this.setMutexBits(1);
        mourningTime = citizen.getRNG().nextInt(MIN_MOURN_RANDOM_TIME) + MIN_MOURN_TIME;
    }

    /**
     * {@inheritDoc}
     * Returns whether the EntityAIBase should begin execution.
     * This will execute if the status for the citizen is set to MOURN
     * It will determine the location of the mourn location and start to move
     * entity toward that location.   Once there it will random move the citizen around.
     * It will determine a nearby citizen and look at that citizen.  To appear they are communicating.
     * If citizen gets to far away then they will move back to the mourn location.
     */
    @Override
    public boolean shouldExecute()
    {
        if (citizen.getDesiredActivity() != DesiredActivity.MOURN)
        {
            return false;
        }

        if ((citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
            || (reachedFinalDestination && checkForRandom()))
        {
            closestEntity = this.citizen.world.findNearestEntityWithinAABB(EntityCitizen.class,
                            citizen.getEntityBoundingBox().grow((double) maxDistanceForPlayer, 3.0D, (double)maxDistanceForPlayer), citizen);
            if (closestEntity == null)
            {
                continueLooking = false;
            }
            return false;
        }
        citizen.getCitizenStatusHandler().setStatus(Status.MOURN);
        return true;
    }

    /**
     * @return a random value of 0 - 100, if 0 return true.
     */
    private boolean checkForRandom()
    {
        return citizen.getRNG().nextInt(100) != 0;
    }

    /**
     * {@inheritDoc}
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !citizen.getNavigator().noPath() || (citizen.getDesiredActivity() == DesiredActivity.MOURN);
    }

    /**
     * {@inheritDoc}
     * Execute a one shot task or start executing a continuous task.
     */
    @Override
    public void startExecuting()
    {
        citizen.getCitizenItemHandler().removeHeldItem();
        citizen.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }

    /**
     * Call this function to get the mourn location
     *
     * @return vec3d of the location to mourn at
     */
    protected Vec3d getMournLocation()
    {
        Vec3d vec3d = null;
        final Colony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony == null || !colony.getBuildingManager().hasTownHall())
        {
            return new Vec3d(citizen.getHomePosition());
        }

        final BlockPos pos = colony.getBuildingManager().getTownHall().getLocation();
        if (pos != null)
        {
            vec3d = new Vec3d(pos);
        }
        return vec3d;
    }

    @Override
    public void resetTask()
    {
        if (citizen.getDesiredActivity() != DesiredActivity.MOURN || !citizen.isMourning())
        {
            reachedFinalDestination = false;
            goingToMourn = false;
            continueLooking = false;
            mourningTime = citizen.getRNG().nextInt(MIN_MOURN_RANDOM_TIME) + MIN_MOURN_TIME;
            xPosition = 0;
            yPosition = 0;
            zPosition = 0;
        }
        super.resetTask();
    }

    @Override
    public void updateTask()
    {
        if (mourningTime > 0)
        {
            mourningTime--;
        }

        if (goingToMourn || closestEntity == null || (closestEntity != null && citizen.getDistanceSq(closestEntity) > (double) (maxDistanceForPlayer * maxDistanceForPlayer)))
        {
            continueLooking = false;
        }
        else if (closestEntity != null)
        {
            continueLooking = true;
        }

        if (goingToMourn && citizen.getDistance(homeLocation.x, homeLocation.y, homeLocation.z) < MIN_DESTINATION_TO_LOCATION)
        {
            reachedFinalDestination = true;
            goingToMourn = false;
        }

        if (mourningTime <= 0)
        {
            citizen.setMourning(false);
        }
        Vec3d vec3d = null;
        if (!reachedFinalDestination)
        {
            goingToMourn = true;
            if (homeLocation == null)
            {
                vec3d = getMournLocation();
                homeLocation = vec3d;
            }
            else
            {
                vec3d = homeLocation;
            }
        }
        else
        {
            if (citizen.getDistance(homeLocation.x, homeLocation.y, homeLocation.z) < MIN_DESTINATION_TO_LOCATION)
            {
                vec3d = RandomPositionGenerator.getLandPos(citizen, 10, 10);
                if (vec3d != null)
                {
                    vec3d = new Vec3d(vec3d.x, BlockPosUtil.getValidHeight(vec3d, CompatibilityUtils.getWorld(citizen)), vec3d.z);
                }
            }
            else
            {
                vec3d = getMournLocation();
                reachedFinalDestination = false;
                goingToMourn = true;
                continueLooking = false;
            }
        }

        if (vec3d != null)
        {
            this.xPosition = vec3d.x;
            this.yPosition = vec3d.y;
            this.zPosition = vec3d.z;
        }


        if (continueLooking && closestEntity != null)
        {
            citizen.getLookHelper().setLookPosition(closestEntity.posX, closestEntity.posY + (double) closestEntity.getEyeHeight(),
                    closestEntity.posZ, (float) citizen.getHorizontalFaceSpeed(), (float) citizen.getVerticalFaceSpeed());
        }
        else
        {
            citizen.getLookHelper().setLookPosition(citizen.posX, citizen.posY - 10, citizen.posZ, (float) citizen.getHorizontalFaceSpeed(),
                    (float) citizen.getVerticalFaceSpeed());
        }
        super.updateTask();
    }

}
