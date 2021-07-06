package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

/**
 * Citizen mourning goal. Has citizens randomly walk around townhall.
 */
public class EntityAIMournCitizen extends Goal
{
    /**
     * Different mourning states.
     */
    public enum MourningState implements IState
    {
        IDLE,
        WALKING_TO_TOWNHALL,
        WANDERING,
        STARING
    }

    /**
     * AI statemachine
     */
    private final TickRateStateMachine<MourningState> stateMachine;

    /**
     * handler to the citizen thisis located
     */
    private final EntityCitizen citizen;

    /**
     * Speed at which the citizen will move around
     */
    private final double speed;

    /**
     * Pointer to the closest citizen to look at.
     */
    private Entity closestEntity;

    /**
     * Constant values of mourning
     */
    private static final int MIN_DESTINATION_TO_LOCATION = 225;
    private static final int AVERAGE_MOURN_TIME = 60 * 5;
    private static final int AVERAGE_STARE_TIME = 10 * 20;

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
        this.setFlags(EnumSet.of(Flag.MOVE));

        stateMachine = new TickRateStateMachine<>(MourningState.IDLE, e -> Log.getLogger().warn("Mourning AI threw exception:", e));

        stateMachine.addTransition(new TickingTransition<>(this::shouldMourn, () -> MourningState.IDLE, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.IDLE, () -> true, this::decide, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WALKING_TO_TOWNHALL, () -> true, this::walkToTownHall, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WANDERING, () -> true, this::wander, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.STARING, () -> true, this::stare, 20));
    }

    /**
     * Check if the citizen should still be mourning.
     * @return true if so.
     */
    private boolean shouldMourn()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.MOURN && this.citizen.getRandom().nextInt(AVERAGE_MOURN_TIME) < 1)
        {
            this.citizen.getCitizenData().getCitizenMournHandler().clearDeceasedCitizen();
            this.citizen.getCitizenData().getCitizenMournHandler().setMourning(false);
            citizen.getCitizenData().setVisibleStatus(null);
            return true;
        }
        return false;
    }

    /**
     * Path to the townhall.
     * @return IDLE again.
     */
    private MourningState walkToTownHall()
    {
        final BlockPos pos = getMournLocation();
        citizen.getNavigation().moveToXYZ(pos.getX(), pos.getY(), pos.getZ(), this.speed);
        return MourningState.IDLE;
    }

    /**
     * Wander around randomly.
     * @return also IDLE again.
     */
    private MourningState wander()
    {
        citizen.getNavigation().moveToRandomPos(10, this.speed);
        return MourningState.IDLE;
    }

    /**
     * State at a random player around.
     * @return Staring if there is a player, else IDLE.
     */
    private MourningState stare()
    {
        if (this.citizen.getRandom().nextInt(AVERAGE_STARE_TIME) < 1)
        {
            closestEntity = null;
            return MourningState.IDLE;
        }

        if (closestEntity == null)
        {
            closestEntity = this.citizen.level.getNearestEntity(EntityCitizen.class,
              EntityPredicate.DEFAULT,
              citizen,
              citizen.getX(),
              citizen.getY(),
              citizen.getZ(),
              citizen.getBoundingBox().inflate(3.0D, 3.0D, 3.0D));

            if (closestEntity == null)
            {
                return MourningState.IDLE;
            }
        }

        citizen.getLookControl().setLookAt(closestEntity.getX(), closestEntity.getY() + (double) closestEntity.getEyeHeight(), closestEntity.getZ(), (float) citizen.getMaxHeadYRot(), (float) citizen.getMaxHeadXRot());
        return MourningState.STARING;
    }

    /**
     * Decide what to do next.
     * @return the next state to go to.
     */
    private MourningState decide()
    {
        if (citizen.getDesiredActivity() != DesiredActivity.MOURN)
        {
            return MourningState.IDLE;
        }

        if (!citizen.getNavigation().isDone())
        {
            return MourningState.IDLE;
        }

        if (citizen.getCitizenStatusHandler().getStatus() != Status.MOURN)
        {
            citizen.getCitizenItemHandler().removeHeldItem();
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.MOURNING);
            citizen.getCitizenStatusHandler().setStatus(Status.MOURN);
            return MourningState.WALKING_TO_TOWNHALL;
        }

        citizen.getLookControl().setLookAt(citizen.getX(), citizen.getY() - 10, citizen.getZ(), (float) citizen.getMaxHeadYRot(),
          (float) citizen.getMaxHeadXRot());

        if (BlockPosUtil.getDistance2D(this.citizen.blockPosition(), getMournLocation()) > MIN_DESTINATION_TO_LOCATION)
        {
            return MourningState.WALKING_TO_TOWNHALL;
        }

        if (this.citizen.getRandom().nextBoolean())
        {
            return MourningState.STARING;
        }

        return MourningState.WANDERING;
    }

    @Override
    public boolean canUse()
    {
        stateMachine.tick();
        return stateMachine.getState() != MourningState.IDLE;
    }

    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    @Override
    public void stop()
    {
        stateMachine.reset();
        citizen.getCitizenData().setVisibleStatus(null);
    }

    /**
     * Call this function to get the mourn location
     *
     * @return blockPos of the location to mourn at
     */
    protected BlockPos getMournLocation()
    {
        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony == null || !colony.getBuildingManager().hasTownHall())
        {
            return citizen.getRestrictCenter();
        }

        return colony.getBuildingManager().getTownHall().getPosition();
    }
}
