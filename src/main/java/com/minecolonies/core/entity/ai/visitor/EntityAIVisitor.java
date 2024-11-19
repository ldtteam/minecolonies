package com.minecolonies.core.entity.ai.visitor;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.states.EntityState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.VisitorData;
import com.minecolonies.core.colony.buildings.DefaultBuildingInstance;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * AI for visitors, they do sometimes nap on their place, sit on their place, randomly walk around inside building outline
 */
public class EntityAIVisitor implements IState
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
     * The tavern building for the citizen
     */
    private DefaultBuildingInstance tavern;

    /**
     * Times out entity action going back to a deciding state
     */
    private int actionTimeoutCounter = 0;

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

        ITickRateStateMachine<IState> stateMachine = entity.getEntityStateController();
        stateMachine.addTransition(new TickingTransition<>(EntityState.INIT, this::isEntityLoaded, () -> VisitorState.IDLE, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.IDLE, () -> true, this::decide, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.WANDERING, this::wander, () -> VisitorState.IDLE, 200));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.WANDERING, this::shouldFight, () -> VisitorState.COMBAT, 200));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.SITTING, this::sit, () -> VisitorState.IDLE, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.SITTING, this::shouldFight, () -> VisitorState.COMBAT, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.COMBAT, this::doFight, () -> VisitorState.IDLE, COMBAT_UPDATE_RATE));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.IDLE, this::reduceTime, stateMachine::getState, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.WANDERING, this::reduceTime, stateMachine::getState, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.SITTING, this::reduceTime, stateMachine::getState, 50));
    }

    /**
     * Reduces the food left on a citizen
     *
     * @return true if done
     */
    private boolean reduceTime()
    {
        citizen.getCitizenData().decreaseSaturation(0.02);
        citizen.getCitizenData().markDirty(20 * 20);
        if (citizen.getCitizenData().getSaturation() <= 0)
        {
            citizen.getCitizenColonyHandler().getColonyOrRegister().getVisitorManager().removeCivilian(citizen.getCitizenData());
            if (tavern != null)
            {
                tavern.getFirstModuleOccurance(TavernBuildingModule.class).removeCitizen(citizen.getCivilianID());
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
            citizen.setLastHurtByMob(null);
            citizen.setTarget(null);
            return true;
        }

        if (citizen.isWorkerAtSiteWithMove(target.blockPosition(), 2) && citizen.hasLineOfSight(target))
        {
            citizen.swing(InteractionHand.MAIN_HAND);
            target.hurt(target.level.damageSources().source(DamageSourceKeys.VISITOR), 10.0f);
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

        citizen.getNavigation().moveToRandomPos(10, 0.6D);
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
        if (tavern != null && (random == 0 || random == 1 && !citizen.getCitizenColonyHandler().getColonyOrRegister().isDay()) && tavern.hasModule(BuildingModules.TAVERN_VISITOR))
        {
            final BlockPos pos = tavern.getModule(BuildingModules.TAVERN_VISITOR).getFreeSitPosition();
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
            citizen.getNavigation().moveToRandomPos(10, 0.6D);
            actionTimeoutCounter = citizen.getCitizenColonyHandler().getColonyOrRegister().isDay() ? citizen.getRandom().nextInt(1000) + 1000 : 300;
            return VisitorState.WANDERING;
        }

        return VisitorState.IDLE;
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

        if (citizen.getNavigation().isInProgress() || citizen.getVehicle() instanceof SittingEntity)
        {
            return false;
        }

        final BlockPos moveTo = ((VisitorData) citizen.getCitizenData()).getSittingPosition();
        if (citizen.isWorkerAtSiteWithMove(moveTo, 1))
        {
            SittingEntity.sitDown(moveTo, citizen, actionTimeoutCounter);
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
        if (citizen.getCitizenColonyHandler().getColonyOrRegister() == null || citizen.getCitizenData() == null || citizen.getCitizenData().getHomeBuilding() == null)
        {
            return false;
        }

        IBuilding building = citizen.getCitizenData().getHomeBuilding();
        if (building.hasModule(BuildingModules.TAVERN_VISITOR))
        {
            tavern = (DefaultBuildingInstance) building;
        }

        ((VisitorData) citizen.getCitizenData()).setSittingPosition(BlockPos.ZERO);

        return WorldUtil.isEntityBlockLoaded(citizen.level, citizen.blockPosition());
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
            target = citizen.getTarget();
            if (target == null)
            {
                target = citizen.getLastHurtByMob();
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
     * Resets saved data of internal logic
     */
    private void resetLogic()
    {
        ((VisitorData) citizen.getCitizenData()).setSittingPosition(BlockPos.ZERO);
    }

    /**
     * Resets the task.
     */
    public void stop()
    {
        resetLogic();
    }
}
