package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends Goal
{
    protected final AbstractEntityCitizen citizen;
    protected final double                speed;
    private final   double                randomModifier;
    private         double                xPosition;
    private         double                yPosition;
    private         double                zPosition;

    /**
     * Instantiates this task.
     *
     * @param citizen        the citizen.
     * @param speed          the speed.
     * @param randomModifier the random modifier for the movement.
     */
    public EntityAICitizenWander(final AbstractEntityCitizen citizen, final double speed, final double randomModifier)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;
        this.randomModifier = randomModifier;
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    /**
     * {@inheritDoc} Returns whether the Goal should begin execution. True when age less than 100, when a random (120) is chosen correctly, and when a citizen is nearby.
     */
    @Override
    public boolean shouldExecute()
    {
        if (isTooOld() || checkForRandom() || citizen.getDesiredActivity() == DesiredActivity.SLEEP || !citizen.getNavigator().noPath())
        {
            return false;
        }

        Vector3d Vector3d = null;
        if (Vector3d == null)
        {
            Vector3d = RandomPositionGenerator.getLandPos(citizen, 10, 7);
            if (Vector3d == null)
            {
                return false;
            }
        }

        Vector3d = new Vector3d(Vector3d.x, BlockPosUtil.getValidHeight(Vector3d, CompatibilityUtils.getWorldFromCitizen(citizen)), Vector3d.z);

        this.xPosition = Vector3d.x;
        this.yPosition = Vector3d.y;
        this.zPosition = Vector3d.z;

        return true;
    }

    /**
     * Returns whether or not the citizen is too old to wander. True when age >= 100.
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
     * {@inheritDoc} Returns whether an in-progress Goal should continue executing.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }

    /**
     * {@inheritDoc} Execute a one shot task or start executing a continuous task.
     */
    @Override
    public void startExecuting()
    {
        citizen.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}
