package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends Goal
{
    protected final AbstractEntityCitizen citizen;
    protected final double                speed;
    private final   double                randomModifier;

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
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    /**
     * {@inheritDoc} Returns whether the Goal should begin execution. True when age less than 100, when a random (120) is chosen correctly, and when a citizen is nearby.
     */
    @Override
    public boolean canUse()
    {
        return !checkForRandom() && citizen.getDesiredActivity() != DesiredActivity.SLEEP && citizen.getNavigation().isDone();
    }

    private boolean checkForRandom()
    {
        return citizen.getRandom().nextInt((int) (randomModifier * 120.0D)) != 0;
    }

    /**
     * {@inheritDoc} Returns whether an in-progress Goal should continue executing.
     */
    @Override
    public boolean canContinueToUse()
    {
        return !citizen.getNavigation().isDone();
    }

    /**
     * {@inheritDoc} Execute a one shot task or start executing a continuous task.
     */
    @Override
    public void start()
    {
        citizen.getNavigation().moveToRandomPos(10, this.speed);
    }

    @Override
    public void stop()
    {
        citizen.getCitizenData().setVisibleStatus(null);
    }
}
