package com.minecolonies.entity.ai.minimal;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

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
     * True when age less than 100, when a random (120) is chosen correctly, and when a citizen is nearby.
     */
    @Override
    public boolean shouldExecute()
    {
        if (isToOld() || checkForRandom())
        {
            return false;
        }
        Vec3d Vec3d = RandomPositionGenerator.findRandomTarget(citizen, 10, 7);
        if (Vec3d == null)
        {
            return false;
        }

        Vec3d = new Vec3d(Vec3d.xCoord, getValidHeight(Vec3d), Vec3d.zCoord);

        this.xPosition = Vec3d.xCoord;
        this.yPosition = Vec3d.yCoord;
        this.zPosition = Vec3d.zCoord;

        return true;
    }

    /**
     * Returns whether or not the citizen is too old to wander
     * True when age >= 100;
     *
     * @return True when age => 100, otherwise false
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
     * Returns the right height for the given position (ground block)
     *
     * @param position Current position of the entity
     * @return Ground level at (position.x, position.z)
     */
    private double getValidHeight(@NotNull Vec3d position)
    {
        double returnHeight = position.yCoord;
        if (position.yCoord < 0)
        {
            returnHeight = 0;
        }

        while (returnHeight >= 1 && citizen.worldObj.isAirBlock(new BlockPos(MathHelper.floor_double(position.xCoord),
                                                                              (int) returnHeight - 1,
                                                                              MathHelper.floor_double(position.zCoord))))
        {
            returnHeight -= 1.0D;
        }

        while (!citizen.worldObj.isAirBlock(new BlockPos(MathHelper.floor_double(position.xCoord), (int) returnHeight, MathHelper.floor_double(position.zCoord))))
        {
            returnHeight += 1.0D;
        }
        return returnHeight;
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
