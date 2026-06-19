package com.minecolonies.core.entity.ai.cavalry;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;

/*
 * A simple override of WaterAvoidingRandomStrollGoal to discourage 
 * cavalry horses from wandering out of stables, if they are in one.
 */
public class CavalryStrollGoal extends WaterAvoidingRandomStrollGoal
{
    private BlockPos stall = null;

    public CavalryStrollGoal(CavalryHorseEntity horse, double speed)
    {
        super(horse, speed);
    }

    /**
     * Returns whether the goal can be used.
     * <p>
     * Conditions to return false:
     * <ul>
     *     <li>The horse is on the client side.</li>
     *     <li>The horse is not an instance of {@link CavalryHorseEntity}.</li>
     *     <li>The horse has a passenger.</li>
     *     <li>The horse has a reservation.</li>
     *     <li>The horse does not have a valid animal data object.</li>
     * </ul>
     * <p>
     * If the goal is usable, the horse will continue to move towards the stable block position.
     * 
     * @return true if the goal can be used, false otherwise
     */
    @Override
    public boolean canUse() 
    {
        if (mob.level().isClientSide) return false;

        return isFreeToRoam() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() 
    {
        return isFreeToRoam() && super.canContinueToUse();
    }

    /**
     * Checks if the horse is free to roam, meaning it has no passenger, no reservation, and has a valid animal data object.
     * 
     * @return true if the horse is free to roam, false otherwise
     */
    protected boolean isFreeToRoam()
    {
        if (!(mob instanceof CavalryHorseEntity horse))
        {
            return false;
        }

        if (mob.getControllingPassenger() != null || horse.hasReservation() || horse.getAnimalData() == null)
        {
            return false;
        }

        return true;
    }

    /**
     * Called when the goal is started. This goal will first check if the horse is in a stable building. 
     * If it is, it will set the stall position to the next available stall position in the building. 
     * If the horse is not in a stable building, or if the stall position is already set, 
     * it will call the superclass method to start the goal.
     */
    @Override
    public void start() 
    {
        if (mob instanceof CavalryHorseEntity horse && horse.isInStable())
        {
            IBuilding building = horse.getStableBuilding();

            if (building instanceof BuildingStable stable && stall == null)
            {
                stall = stable.getNextStallPosition();
            }
        }

        super.start();
    }

    /**
     * Returns the position of the horse to move to. If the horse is in a stable, this will return the next available stall position.
     * Otherwise, it will call the superclass method to get the position.
     * 
     * @return the position to move to
     */
    @Override
    protected Vec3 getPosition()
    {
        if (mob instanceof CavalryHorseEntity horse)
        {
            if (horse.isInStable() && stall != null) 
            {
                return new Vec3(stall.getX() + 0.5, stall.getY() + 0.5, stall.getZ() + 0.5);
            }
            else
            {
                // If the horse is not in a stable any more, reset the cached stall position
                stall = null;
            }
        }

        return super.getPosition();
    }

    /**
     * Resets the stall position to null and stops the goal.
     */
    @Override
    public void stop()
    {
        stall = null;
        super.stop();
    }
}
