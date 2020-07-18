package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.colony.VisitorData;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTavern;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.citizen.VisitorCitizen;
import com.minecolonies.coremod.util.NamedDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * AI for visitors, they do sometimes nap on their place, sit on their place, randomly walk around inside building outline
 */
public class EntityAIVisitor extends Goal
{
    /**
     * Update interval during combat
     */
    private static final int COMBAT_UPDATE_RATE = 20;

    /**
     * States of the visitor AI
     */
    public enum VisitorState implements IState
    {
        INIT,
        IDLE,
        SLEEPING,
        SITTING,
        COMBAT,
        WANDERING
    }

    /**
     * The visitor entity we are attached to.
     */
    private final VisitorCitizen citizen;

    /**
     * This AI's state changer.
     */
    private final ITickRateStateMachine<VisitorState> stateMachine;

    /**
     * The tavern building for the citizen
     */
    private BuildingTavern tavern;

    /**
     * Times out entity action going back to a deciding state
     */
    private int actionTimeoutCounter = 0;

    /**
     * The current wander position
     */
    private BlockPos wanderPos = null;

    /**
     * The current attack target
     */
    private Entity target = null;

    /**
     * Constructor.
     *
     * @param entity current entity.
     */
    public EntityAIVisitor(@NotNull final AbstractEntityCitizen entity)
    {
        super();
        this.citizen = (VisitorCitizen) entity;
        stateMachine = new TickRateStateMachine<>(VisitorState.INIT, this::onException);

        stateMachine.addTransition(new TickingTransition<>(VisitorState.INIT, this::isEntityLoaded, () -> VisitorState.IDLE, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.IDLE, () -> true, this::decide, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.WANDERING, this::wander, () -> VisitorState.IDLE, 200));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.WANDERING, this::shouldFight, () -> VisitorState.COMBAT, 200));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.SITTING, this::sit, () -> VisitorState.IDLE, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.SITTING, this::shouldFight, () -> VisitorState.COMBAT, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.COMBAT, this::doFight, () -> VisitorState.IDLE, COMBAT_UPDATE_RATE));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.IDLE, this::reduceTime, stateMachine::getState, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.WANDERING, this::reduceTime, stateMachine::getState, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.SITTING, this::reduceTime, stateMachine::getState, 50));
        setMutexFlags(EnumSet.of(Flag.JUMP));
    }

    /**
     * Reduces the food left on a citizen
     *
     * @return true if done
     */
    private boolean reduceTime()
    {
        citizen.getCitizenData().decreaseSaturation(0.02);
        citizen.getCitizenData().markDirty();
        if (citizen.getCitizenData().getSaturation() <= 0)
        {
            citizen.getCitizenColonyHandler().getColony().getVisitorManager().removeCivilian(citizen.getCitizenData());
            if (tavern != null)
            {
                tavern.removeCitizen(citizen.getCivilianID());
            }
            return true;
        }
        return false;
    }

    /**
     * Does the fighting logic
     *
     * @return true if done
     */
    private boolean doFight()
    {
        if (target == null || !target.isAlive() || (actionTimeoutCounter -= COMBAT_UPDATE_RATE) <= 0)
        {
            target = null;
            citizen.setRevengeTarget(null);
            citizen.setAttackTarget(null);
            return true;
        }

        if (citizen.isWorkerAtSiteWithMove(new BlockPos(target.getPositionVec()), 2) && citizen.canEntityBeSeen(target))
        {
            citizen.swingArm(Hand.MAIN_HAND);
            target.attackEntityFrom(new NamedDamageSource(citizen.getName().getString(), citizen), 10.0f);
        }

        return false;
    }

    /**
     * Wander action
     *
     * @return true if done
     */
    private boolean wander()
    {
        if ((actionTimeoutCounter -= 50) <= 0)
        {
            return true;
        }

        if (wanderPos == null || citizen.isWorkerAtSiteWithMove(wanderPos, 3))
        {
            generateWanderPos();
        }
        return false;
    }

    private boolean shouldFight()
    {
        if (getTarget() != null)
        {
            actionTimeoutCounter = 30 * 20;
            return true;
        }
        return false;
    }

    /**
     * Decides on the next activity
     *
     * @return next state
     */
    private VisitorState decide()
    {
        if (shouldFight())
        {
            return VisitorState.COMBAT;
        }

        final int random = citizen.getRandom().nextInt(5);
        if (tavern != null && (random == 0 || random == 1 && !citizen.getCitizenColonyHandler().getColony().isDay()))
        {
            final BlockPos pos = tavern.getFreeSitPosition();
            if (pos != null)
            {
                ((VisitorData) citizen.getCitizenData()).setSittingPosition(pos);
                citizen.isWorkerAtSiteWithMove(pos, 1);
                actionTimeoutCounter = citizen.getRandom().nextInt(2500) + 3000;
                return VisitorState.SITTING;
            }
        }
        else if (random == 2)
        {
            generateWanderPos();
            actionTimeoutCounter = citizen.getCitizenColonyHandler().getColony().isDay() ? citizen.getRandom().nextInt(1000) + 1000 : 300;
            return VisitorState.WANDERING;
        }

        return VisitorState.IDLE;
    }

    /**
     * Gets a new wander position
     */
    private void generateWanderPos()
    {
        final Vector3d vec3d = RandomPositionGenerator.getLandPos(citizen, 10, 7);

        if (vec3d != null && WorldUtil.isEntityBlockLoaded(citizen.world, new BlockPos(vec3d.x, vec3d.y, vec3d.z)))
        {
            wanderPos = new BlockPos(vec3d.x, BlockPosUtil.getValidHeight(vec3d, CompatibilityUtils.getWorldFromCitizen(citizen)), vec3d.z);
        }
    }

    /**
     * Sitting activity, finds a free spot in the tavern and sits down there
     *
     * @return true if wants to sit
     */
    private boolean sit()
    {
        if ((actionTimeoutCounter -= 50) <= 0)
        {
            ((VisitorData) citizen.getCitizenData()).setSittingPosition(BlockPos.ZERO);
            return true;
        }

        final BlockPos moveTo = ((VisitorData) citizen.getCitizenData()).getSittingPosition();
        if (citizen.isWorkerAtSiteWithMove(moveTo, 1))
        {
            if (citizen.getRidingEntity() == null)
            {
                final SittingEntity entity = (SittingEntity) ModEntities.SITTINGENTITY.create(citizen.world);
                entity.setPosition(moveTo.getX() + 0.5, moveTo.getY() - 0.4, moveTo.getZ() + 0.5);
                entity.setMaxLifeTime(actionTimeoutCounter);
                citizen.world.addEntity(entity);
                citizen.startRiding(entity);
                citizen.getNavigator().clearPath();
            }
        }
        return false;
    }

    /**
     * Whether the entity is in a ticked chunk
     *
     * @return true if loaded
     */
    private boolean isEntityLoaded()
    {
        if (citizen.getCitizenColonyHandler().getColony() == null || citizen.getCitizenData() == null || citizen.getCitizenData().getHomeBuilding() == null)
        {
            return false;
        }

        IBuilding building = citizen.getCitizenData().getHomeBuilding();
        if (building != null)
        {
            tavern = (BuildingTavern) building;
        }

        ((VisitorData) citizen.getCitizenData()).setSittingPosition(BlockPos.ZERO);

        return WorldUtil.isEntityBlockLoaded(citizen.world, citizen.getPosition());
    }

    /**
     * Returns a target we need to attack or null
     *
     * @return target
     */
    private Entity getTarget()
    {
        if (target == null)
        {
            target = citizen.getAttackTarget();
            if (target == null)
            {
                target = citizen.getRevengeTarget();
            }
        }
        return target;
    }

    /**
     * Handles any exceptions for this AI.
     *
     * @param e exception to handle
     */
    private void onException(final RuntimeException e)
    {
        Log.getLogger().warn("Visitor AI of:" + citizen.getName() + " threw an Exception:", e);
    }

    /**
     * Returns whether the Goal should begin execution of avoiding.
     */
    @Override
    public boolean shouldExecute()
    {
        return true;
    }

    /**
     * Returns whether an in-progress Goal should continue executing.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        stateMachine.tick();
        return true;
    }

    /**
     * Resets saved data of internal logic
     */
    private void resetLogic()
    {
        ((VisitorData) citizen.getCitizenData()).setSittingPosition(BlockPos.ZERO);
    }

    /**
     * Resets the task.
     */
    @Override
    public void resetTask()
    {
        stateMachine.reset();
        resetLogic();
    }
}
