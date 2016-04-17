package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class EntityAICitizenWander extends EntityAIBase
{
    private EntityCitizen citizen;
    private double        xPosition;
    private double        yPosition;
    private double        zPosition;
    private double        speed;

    public EntityAICitizenWander(EntityCitizen citizen, double speed)
    {
        this.citizen = citizen;
        this.speed = speed;
        this.setMutexBits(1);
    }

    /**
     * {@inheritDoc}
     * Returns whether the EntityAIBase should begin execution.
     * True when age < 100, when a random (120) is chosen correctly, and when a citizen is nearby
     */
    @Override
    public boolean shouldExecute()
    {
        if(isToOld() || checkForRandom())
        {
            return false;
        }
        Vec3 vec3 = RandomPositionGenerator.findRandomTarget(citizen, 10, 7);
        if(vec3 == null)
        {
            return false;
        }

        vec3.yCoord = getValidHeight(vec3);

        this.xPosition = vec3.xCoord;
        this.yPosition = vec3.yCoord;
        this.zPosition = vec3.zCoord;

        return true;
    }

    /**
     * Returns the right height for the given position (ground block)
     * @param position      Current position of the entity
     * @return              Ground level at (position.x, position.z)
     */
    private double getValidHeight(Vec3 position)
    {
        double returnHeight = position.yCoord;
        if(position.yCoord < 0)
        {
            returnHeight= 0;
        }

        while(returnHeight >= 1 && citizen.worldObj.isAirBlock(new BlockPos(MathHelper.floor_double(position.xCoord), (int) returnHeight - 1, MathHelper.floor_double(position.zCoord))))
        {
            returnHeight -= 1.0D;
        }

        while(!citizen.worldObj.isAirBlock(new BlockPos(MathHelper.floor_double(position.xCoord), (int) returnHeight, MathHelper.floor_double(position.zCoord))))
        {
            returnHeight += 1.0D;
        }
        return returnHeight;
    }

    /**
     * Returns whether or not the citizen is too old to wander
     * True when age >= 100;
     *
     * @return      True when age => 100, otherwise false
     */
    private boolean isToOld()
    {
        return citizen.getAge() >= 100;
    }

    private boolean checkForRandom()
    {
        return citizen.getRNG().nextInt(120) != 0;
    }

    /**
     * {@inheritDoc}
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }

    /**
     * {@inheritDoc}
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        citizen.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}