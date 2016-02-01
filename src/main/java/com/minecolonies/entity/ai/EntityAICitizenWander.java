package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
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
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if(citizen.getAge() >= 100)
        {
            return false;
        }
        if(citizen.getRNG().nextInt(120) != 0)
        {
            return false;
        }
        Vec3 vec3 = RandomPositionGenerator.findRandomTarget(citizen, 10, 7);
        if(vec3 == null)
        {
            return false;
        }

        if(vec3.yCoord < 0)
        {
            vec3.yCoord = 0;
        }

        while(vec3.yCoord >= 1 && citizen.worldObj.isAirBlock(MathHelper.floor_double(vec3.xCoord), (int) vec3.yCoord - 1, MathHelper.floor_double(vec3.zCoord)))
        {
            vec3.yCoord -= 1.0D;
        }

        while(!citizen.worldObj.isAirBlock(MathHelper.floor_double(vec3.xCoord), (int) vec3.yCoord, MathHelper.floor_double(vec3.zCoord)))
        {
            vec3.yCoord += 1.0D;
        }

        this.xPosition = vec3.xCoord;
        this.yPosition = vec3.yCoord;
        this.zPosition = vec3.zCoord;

        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        citizen.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}