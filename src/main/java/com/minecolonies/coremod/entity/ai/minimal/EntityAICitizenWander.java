package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.entity.IEntityCitizen;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends EntityAIBase
{
    protected final IEntityCitizen citizen;
    protected final double        speed;
    private         double        xPosition;
    private         double        yPosition;
    private         double        zPosition;
    private   final double        randomModifier;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     * @param speed   the speed.
     */
    public EntityAICitizenWander(final IEntityCitizen citizen, final double speed, final double randomModifier)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;
        this.randomModifier = randomModifier;
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
        if (isTooOld() || checkForRandom() || citizen.getDesiredActivity() == DesiredActivity.SLEEP || !citizen.getNavigator().noPath())
        {
            return false;
        }

        Vec3d vec3d = null;
        if(vec3d == null)
        {
            vec3d = RandomPositionGenerator.getLandPos((EntityCreature) citizen, 10, 7);
            if (vec3d == null)
            {
                return false;
            }
        }

        vec3d = new Vec3d(vec3d.x, BlockPosUtil.getValidHeight(vec3d, CompatibilityUtils.getWorldFromCitizen(citizen)), vec3d.z);

        this.xPosition = vec3d.x;
        this.yPosition = vec3d.y;
        this.zPosition = vec3d.z;

        return true;
    }

    /**
     * Returns whether or not the citizen is too old to wander.
     * True when age >= 100.
     *
     * @return True when age => 100, otherwise false.
     */
    private boolean isTooOld()
    {
        return citizen.getGrowingAge() >= 100;
    }

    private boolean checkForRandom()
    {
        return citizen.getRNG().nextInt((int) (randomModifier * 120.0D)) != 0;
    }

    /**
     * {@inheritDoc}
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public boolean shouldContinueExecuting()
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
