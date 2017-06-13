package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends EntityAIBase
{
    private final EntityCitizen citizen;
    private       double        xPosition;
    private       double        yPosition;
    private       double        zPosition;
    private final double        speed;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     * @param speed   the speed.
     */
    public EntityAICitizenWander(final EntityCitizen citizen, final double speed)
    {
        super();
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
        if (isToOld() || checkForRandom() || citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP)
        {
            return false;
        }

        Vec3d vec3d = RandomPositionGenerator.findRandomTarget(citizen, 10, 7);
        if (vec3d == null)
        {
            return false;
        }

        vec3d = new Vec3d(vec3d.xCoord, getValidHeight(vec3d), vec3d.zCoord);

        this.xPosition = vec3d.xCoord;
        this.yPosition = vec3d.yCoord;
        this.zPosition = vec3d.zCoord;

        return true;
    }

    /**
     * Returns whether or not the citizen is too old to wander.
     * True when age >= 100.
     *
     * @return True when age => 100, otherwise false.
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
     * Returns the right height for the given position (ground block).
     *
     * @param position Current position of the entity.
     * @return Ground level at (position.x, position.z).
     */
    private double getValidHeight(@NotNull final Vec3d position)
    {
        double returnHeight = position.yCoord;
        if (position.yCoord < 0)
        {
            returnHeight = 0;
        }

        while (returnHeight >= 1 && CompatibilityUtils.getWorld(citizen).isAirBlock(new BlockPos(MathHelper.floor_double(position.xCoord),
                                                                              (int) returnHeight,
                                                                              MathHelper.floor_double(position.zCoord))))
        {
            returnHeight -= 1.0D;
        }

        while (!CompatibilityUtils.getWorld(citizen).isAirBlock(
              new BlockPos(MathHelper.floor_double(position.xCoord), (int) returnHeight, MathHelper.floor_double(position.zCoord))))
        {
            returnHeight += 1.0D;
        }
        return returnHeight;
    }

    /**
     * {@inheritDoc}
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }

    /**
     * {@inheritDoc}
     * Execute a one shot task or start executing a continuous task.
     */
    @Override
    public void startExecuting()
    {
        citizen.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}
